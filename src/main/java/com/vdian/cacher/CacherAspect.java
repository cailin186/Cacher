package com.vdian.cacher;

import com.google.common.base.Preconditions;
import com.vdian.cacher.domain.CacheKeyHolder;
import com.vdian.cacher.domain.MethodInfoHolder;
import com.vdian.cacher.domain.Pair;
import com.vdian.cacher.jmx.HitRateRecordMXBean;
import com.vdian.cacher.jmx.HitRateRecordMXBeanImpl;
import com.vdian.cacher.manager.CacheManager;
import com.vdian.cacher.reader.CacheReader;
import com.vdian.cacher.reader.MultiCacheReader;
import com.vdian.cacher.reader.SingleCacheReader;
import com.vdian.cacher.support.cache.NoOpCache;
import com.vdian.cacher.utils.CacherSwitcher;
import com.vdian.cacher.utils.CacherUtils;
import com.vdian.cacher.utils.KeyComposeUtil;
import com.vdian.cacher.utils.MethodInfoCache;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.management.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author jifang
 * @since 2016/11/2 下午2:34.
 */
@Aspect
@SuppressWarnings("unchecked")
public class CacherAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger("com.vdian.cacher");

    private static final String DEFAULT = "default";

    private CacheReader singleCacheReader;

    private CacheReader multiCacheReader;

    private CacheManager cacheManager;

    private volatile boolean open;

    public CacherAspect() {
        this(Collections.singletonMap(DEFAULT, (ICache) new NoOpCache()));
    }

    public CacherAspect(Map<String, ICache> caches) {
        this(caches, true);
    }

    public CacherAspect(Map<String, ICache> caches, boolean open) {
        initCaches(caches);
        this.cacheManager = new CacheManager(caches);
        this.singleCacheReader = new SingleCacheReader(cacheManager);
        this.multiCacheReader = new MultiCacheReader(cacheManager);
        this.open = open;
    }

    @PostConstruct
    public void startUp()
            throws MalformedObjectNameException,
            NotCompliantMBeanException,
            InstanceAlreadyExistsException,
            MBeanRegistrationException {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        HitRateRecordMXBean mxBean = new HitRateRecordMXBeanImpl();
        mBeanServer.registerMBean(mxBean, new ObjectName("com.vdian.cacher:name=HitRate"));
    }

    @Around("@annotation(com.vdian.cacher.Cached)")
    public Object readCache(ProceedingJoinPoint pjp) throws Throwable {
        Method method = CacherUtils.getMethod(pjp);
        Cached cached = method.getAnnotation(Cached.class);

        Object result;
        if (CacherSwitcher.isSwitchOn(open, cached, method, pjp.getArgs())) {
            long start = System.currentTimeMillis();

            Pair<CacheKeyHolder, MethodInfoHolder> pair = MethodInfoCache.getMethodInfo(method);
            CacheKeyHolder cacheKeyHolder = pair.getLeft();
            MethodInfoHolder methodInfoHolder = pair.getRight();

            // multi
            if (cacheKeyHolder.isMulti()) {
                result = multiCacheReader.read(cacheKeyHolder, cached, pjp, methodInfoHolder);
            } else {
                result = singleCacheReader.read(cacheKeyHolder, cached, pjp, methodInfoHolder);
            }
            LOGGER.info("cacher [{}] total cost [{}] ms", cached.cache(), System.currentTimeMillis() - start);
        } else {
            result = pjp.proceed();
        }

        return result;
    }

    @After("@annotation(com.vdian.cacher.Invalidate)")
    public void removeCache(JoinPoint pjp) throws Throwable {
        Method method = CacherUtils.getMethod(pjp);
        Invalidate invalidate = method.getAnnotation(Invalidate.class);

        if (CacherSwitcher.isSwitchOn(open, invalidate, method, pjp.getArgs())) {
            long start = System.currentTimeMillis();
            Pair<CacheKeyHolder, MethodInfoHolder> pair = MethodInfoCache.getMethodInfo(method);
            CacheKeyHolder holder = pair.getLeft();

            if (holder.isMulti()) {
                Map[] keyIdPair = KeyComposeUtil.toMultiKey(holder, invalidate.separator(), pjp.getArgs());
                Set<String> keys = ((Map<String, Object>) keyIdPair[1]).keySet();
                cacheManager.remove(invalidate.cache(), keys.toArray(new String[keys.size()]));

                LOGGER.info("multi cache clear, keys: {}", keys);

            } else {
                String key = KeyComposeUtil.toSingleKey(pair.getLeft(), invalidate.separator(), pjp.getArgs());
                cacheManager.remove(invalidate.cache(), key);

                LOGGER.info("single cache clear, key: {}", key);
            }

            LOGGER.info("cacher [{}] clear cost [{}] ms", invalidate.cache(), System.currentTimeMillis() - start);
        }
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    private void initCaches(Map<String, ICache> caches) {
        Preconditions.checkArgument(!caches.isEmpty(), "at least one ICache implement");

        if (caches.get(DEFAULT) == null) {
            ICache cache = caches.values().iterator().next();
            caches.put(DEFAULT, cache);
        }
    }
}

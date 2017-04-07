package com.vdian.cacher.reader;

import com.vdian.cacher.Cached;
import com.vdian.cacher.config.Inject;
import com.vdian.cacher.config.Singleton;
import com.vdian.cacher.constant.Constant;
import com.vdian.cacher.domain.CacheKeyHolder;
import com.vdian.cacher.domain.MethodInfoHolder;
import com.vdian.cacher.manager.CacheManager;
import com.vdian.cacher.utils.KeyPatternsCombineUtil;
import com.vdian.cacher.utils.KeysCombineUtil;
import org.aspectj.lang.ProceedingJoinPoint;

import static com.vdian.cacher.jmx.RecordMXBean.HIT_COUNT_MAP;
import static com.vdian.cacher.jmx.RecordMXBean.REQUIRE_COUNT_MAP;

/**
 * @author jifang
 * @since 2016/11/5 下午3:10.
 */
@Singleton
public class SingleCacheReader implements CacheReader {

    @Inject
    private CacheManager cacheManager;

    @Override
    public Object read(CacheKeyHolder holder, Cached cached, ProceedingJoinPoint pjp, MethodInfoHolder ret) throws Throwable {

        String key = KeysCombineUtil.toSingleKey(holder, cached.separator(), pjp.getArgs());
        String keyPattern = KeyPatternsCombineUtil.getKeyPattern(holder, cached.separator());

        Object result = cacheManager.readSingle(cached.cache(), key);

        doRecord(result, key, keyPattern);
        // not hit
        if (result == null) {
            // write cache
            result = pjp.proceed();
            cacheManager.writeSingle(cached.cache(), key, result, cached.expire());
        }

        return result;
    }

    private void doRecord(Object result, String key, String keyPattern) {
        String rate;
        if (result == null) {
            rate = "0/1";
        } else {
            rate = "1/1";
            HIT_COUNT_MAP.get(Constant.TOTAL_KEY).incrementAndGet();
            HIT_COUNT_MAP.get(keyPattern).incrementAndGet();
        }
        REQUIRE_COUNT_MAP.get(Constant.TOTAL_KEY).incrementAndGet();
        REQUIRE_COUNT_MAP.get(keyPattern).incrementAndGet();

        LOGGER.info("single cache hit rate: {}, key: {}", rate, key);
    }
}

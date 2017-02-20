package com.vdian.cacher.reader;

import com.vdian.cacher.Cached;
import com.vdian.cacher.domain.CacheKeyHolder;
import com.vdian.cacher.domain.MethodInfoHolder;
import com.vdian.cacher.manager.CacheManager;
import com.vdian.cacher.utils.KeyComposeUtil;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author jifang
 * @since 2016/11/5 下午3:10.
 */
public class SingleCacheReader implements CacheReader {

    private CacheManager cacheManager;

    public SingleCacheReader(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public Object read(CacheKeyHolder holder, Cached cached, ProceedingJoinPoint pjp, MethodInfoHolder ret) throws Throwable {

        String key = KeyComposeUtil.toSingleKey(holder, cached.separator(), pjp.getArgs());
        Object result = cacheManager.readSingle(cached.cache(), key);

        // not hit
        String rate;
        if (result == null) {
            rate = "0/1";

            // write cache
            result = pjp.proceed();
            cacheManager.writeSingle(cached.cache(), key, result, cached.expire());
        } else {
            rate = "1/1";
        }

        LOGGER.info("single cache hit rate: {}, key: {}", rate, key);

        return result;
    }
}

package com.vdian.cacher.reader;

import com.vdian.cacher.Cached;
import com.vdian.cacher.constant.CacherConstant;
import com.vdian.cacher.domain.CacheKeyHolder;
import com.vdian.cacher.domain.MethodInfoHolder;
import com.vdian.cacher.manager.CacheManager;
import com.vdian.cacher.utils.KeyComposeUtil;
import com.vdian.cacher.utils.KeyPatternCache;
import org.aspectj.lang.ProceedingJoinPoint;

import static com.vdian.cacher.jmx.HitRateRecordMXBean.HIT_COUNT_MAP;
import static com.vdian.cacher.jmx.HitRateRecordMXBean.REQUIRE_COUNT_MAP;

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
        String keyPattern = KeyPatternCache.getKeyPattern(holder, cached.separator());

        Object result = cacheManager.readSingle(cached.cache(), key);

        recordResult(result, key, keyPattern);
        // not hit
        if (result == null) {
            // write cache
            result = pjp.proceed();
            cacheManager.writeSingle(cached.cache(), key, result, cached.expire());
        }


        return result;
    }

    private void recordResult(Object result, String key, String keyPattern) {
        String rate;
        if (result == null) {
            rate = "0/1";
        } else {
            rate = "1/1";
            HIT_COUNT_MAP.get(CacherConstant.TOTAL_KEY).incrementAndGet();
            HIT_COUNT_MAP.get(keyPattern).incrementAndGet();
        }
        REQUIRE_COUNT_MAP.get(CacherConstant.TOTAL_KEY).incrementAndGet();
        REQUIRE_COUNT_MAP.get(keyPattern).incrementAndGet();

        LOGGER.info("single cache hit rate: {}, key: {}", rate, key);
    }
}

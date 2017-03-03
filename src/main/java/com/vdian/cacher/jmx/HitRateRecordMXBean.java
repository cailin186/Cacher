package com.vdian.cacher.jmx;

import com.vdian.cacher.utils.CacherUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author jifang
 * @since 2017/3/2 上午11:48.
 */
public interface HitRateRecordMXBean {

    Map<String, AtomicLong> HIT_COUNT_MAP =
            CacherUtils.createAtomicMapProxy(new ConcurrentHashMap<String, AtomicLong>());

    Map<String, AtomicLong> REQUIRE_COUNT_MAP =
            CacherUtils.createAtomicMapProxy(new ConcurrentHashMap<String, AtomicLong>());

    Map<String, Rate> getHitRate();

    void clearPatternRate(String pattern);

    void clearAllRate();

    class Rate {
        private long hitCount;

        private long requireCount;

        private String rate;

        public Rate(long hitCount, long requireCount, String rate) {
            this.hitCount = hitCount;
            this.requireCount = requireCount;
            this.rate = rate;
        }

        public long getHitCount() {
            return hitCount;
        }

        public long getRequireCount() {
            return requireCount;
        }

        public String getRate() {
            return rate;
        }
    }
}

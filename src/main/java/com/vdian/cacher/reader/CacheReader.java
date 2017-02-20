package com.vdian.cacher.reader;

import com.vdian.cacher.Cached;
import com.vdian.cacher.domain.CacheKeyHolder;
import com.vdian.cacher.domain.MethodInfoHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jifang
 * @since 2016/11/5 下午3:22.
 */
public interface CacheReader {

    Logger LOGGER = LoggerFactory.getLogger("com.vdian.cacher");

    Object read(CacheKeyHolder rule, Cached cached, ProceedingJoinPoint pjp, MethodInfoHolder ret) throws Throwable;
}

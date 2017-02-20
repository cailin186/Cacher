package com.vdian.cacher.reader;

import com.vdian.cacher.Cached;
import com.vdian.cacher.domain.BatchReadResult;
import com.vdian.cacher.domain.CacheKeyHolder;
import com.vdian.cacher.domain.MethodInfoHolder;
import com.vdian.cacher.manager.CacheManager;
import com.vdian.cacher.utils.KeyComposeUtil;
import com.vdian.cacher.utils.KeyValueConverts;
import com.vdian.cacher.utils.ResultMergeUtils;
import com.vdian.cacher.utils.ResultTranslateUtils;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author jifang
 * @since 2016/11/5 下午3:11.
 */
@SuppressWarnings("unchecked")
public class MultiCacheReader implements CacheReader {

    private CacheManager cacheManager;

    public MultiCacheReader(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public Object read(CacheKeyHolder rule, Cached cached, ProceedingJoinPoint pjp, MethodInfoHolder ret) throws Throwable {

        // compose keys
        Map[] pair = KeyComposeUtil.toMultiKey(rule, cached.separator(), pjp.getArgs());

        Map<String, Object> keyIdMap = pair[1];

        // request cache
        Set<String> keys = keyIdMap.keySet();
        BatchReadResult batchReadResult = cacheManager.readBatch(cached.cache(), keys);
        Set<String> missedKeys = batchReadResult.getMissKeys();
        LOGGER.info("multi cache hit rate: {}/{}, missed keys: {}",
                keys.size() - missedKeys.size(),
                keys.size(),
                missedKeys);

        Object result;
        // have miss keys : part hit || all not hit
        if (missedKeys.size() > 0) {
            result = handlePartHit(pjp, batchReadResult, rule, ret, cached, pair);
        }
        // no miss keys : all hits || empty key
        else {
            Map<String, Object> keyValueMap = batchReadResult.getHitKeyValueMap();
            result = handleFullHit(pjp, keyValueMap, ret, keyIdMap);
        }

        return result;
    }

    private Object handlePartHit(ProceedingJoinPoint pjp, BatchReadResult batchReadResult,
                                 CacheKeyHolder rule, MethodInfoHolder ret, Cached cached,
                                 Map[] pair) throws Throwable {

        Map<Object, String> id_key = pair[0];
        Map<String, Object> key_id = pair[1];

        Set<String> missKeys = batchReadResult.getMissKeys();
        Map<String, Object> hitKeyValueMap = batchReadResult.getHitKeyValueMap();

        // invoke method use missed keys
        Object[] missArgs = toMissArgs(missKeys, key_id, pjp.getArgs(), rule.getMultiIndex());
        Object proceed = pjp.proceed(missArgs);

        Object result;
        if (proceed != null) {
            Class<?> returnType = proceed.getClass();
            ret.setType(returnType);

            if (Map.class.isAssignableFrom(returnType)) {
                Map proceedIdValueMap = (Map) proceed;

                Map<String, Object> keyValueMap = KeyValueConverts.idValueMap2KeyValue(proceedIdValueMap, id_key);
                // write proceed map to cache
                cacheManager.writeBatch(cached.cache(), keyValueMap, cached.expire());

                // merge map by key-id-map order
                result = ResultMergeUtils.mapMerge(key_id, returnType, proceedIdValueMap, hitKeyValueMap);
            } else {
                Collection proceedCollection = (Collection) proceed;
                String idExp = rule.getIdentifier();

                Map<String, Object> keyValueMap = KeyValueConverts.collection2KeyValue(proceedCollection, idExp, id_key);

                cacheManager.writeBatch(cached.cache(), keyValueMap, cached.expire());

                result = ResultMergeUtils.collectionMerge(key_id.keySet(), returnType, keyValueMap, hitKeyValueMap);
            }
        } else {
            // read as full hit
            result = handleFullHit(pjp, hitKeyValueMap, ret, key_id);
        }

        return result;
    }

    private Object handleFullHit(ProceedingJoinPoint pjp, Map<String, Object> keyValueMap,
                                 MethodInfoHolder ret, Map<String, Object> keyIdMap)
            throws Throwable {

        Object result;
        Class<?> returnType = ret.getType();

        // when method return type not cached. case: full hit when system restart
        if (returnType == null) {
            result = pjp.proceed();
            // catch return type for next time
            if (result != null) {
                ret.setType(result.getClass());
            }
        } else {
            if (ret.isCollection()) {
                result = ResultTranslateUtils.toCollection(returnType, keyValueMap.values());
            } else {
                result = ResultTranslateUtils.toMap(returnType, keyValueMap, keyIdMap);
            }
        }

        return result;
    }

    private Object[] toMissArgs(Set<String> missKeys, Map<String, Object> keyIdMap,
                                Object[] args, int batchIndex)
            throws
            NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {

        Collection<Object> missIds = new ArrayList<>(missKeys.size());
        for (String key : missKeys) {
            Object id = keyIdMap.get(key);
            missIds.add(id);
        }

        Object batchArg = args[batchIndex];
        Class<?> batchArgClass = batchArg.getClass();

        args[batchIndex] = batchArgClass.getConstructor(Collection.class).newInstance(missIds);

        return args;
    }
}

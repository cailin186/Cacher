package com.vdian.cacher.service.impl;


import com.vdian.cacher.CacheKey;
import com.vdian.cacher.Cached;
import com.vdian.cacher.Invalidate;
import com.vdian.cacher.domain.User;
import com.vdian.cacher.enums.Expire;
import com.vdian.cacher.service.UserService;

import java.util.*;

/**
 * @author jifang
 * @since 16/3/20 下午5:49.
 */
public class UserServiceImpl implements UserService {

    /**
     * multi
     ***/
    @Override
    @Cached(cache = "vRedisCluster", prefix = "prefix2", expire = Expire.FOREVER)
    public Map<Integer, User> returnMap(@CacheKey(prefix = "app:") String app, @CacheKey(prefix = "id:", multi = true) List<Integer> ids, Object noKey) {
        Map<Integer, User> map = new TreeMap<>();
        for (Integer id : ids) {
            map.put(id, new User(id, "name" + id, new Date(), id, noKey.toString()));
        }
        return map;
    }

    @Override
    @Invalidate(cache = "vRedisCluster", prefix = "prefix", separator = "~")
    public void multiInvalid(@CacheKey(prefix = "app:") String apps, @CacheKey(prefix = "id:", multi = true) List<Integer> ids) {
    }

    @Override
    @Cached(cache = "vRedisCluster", prefix = "prefix", separator = "~")
    public List<User> returnList(@CacheKey(prefix = "ids:", multi = true, identifier = "id") List<Integer> ids, String name, Object non) {
        List<User> list = new LinkedList<>();
        for (int id : ids) {
            User user = new User(id, name, new Date(), id, non.toString());
            list.add(user);
        }

        return list;
    }

    @Override
    @Invalidate(cache = "vRedisCluster", prefix = "prefix", separator = "~")
    public void batchUpdateList(@CacheKey(prefix = "ids:", multi = true, expression = "id") List<User> users) {
    }

    @Cached
    public User singleKey(@CacheKey(prefix = "id:") int id, String name, Object non) {
        return new User(id, name, new Date(), 1, "山东-德州");
    }

    @Override
    @Invalidate
    public void singleRemove(@CacheKey(prefix = "id:") int id, String name, Object non) {
    }

    @Override
    @Invalidate
    public void updateUser(@CacheKey(prefix = "id:", expression = "id") User user, @CacheKey(prefix = "second:") String name, Object non) {
    }

    /**
     * ops
     */
    @Cached(prefix = "total")
    @Override
    public void noParam() {

    }

    @Cached(prefix = "total")
    @Override
    public void noCacheKey(Object o) {

    }

    @Cached
    @Override
    public void wrongMultiParam(@CacheKey(multi = true) Object o) {

    }

    @Cached
    @Override
    public Map<Integer, Object> wrongIdentifier(@CacheKey(multi = true, identifier = "id") List<Integer> ids) {
        return null;
    }

    @Cached
    @Override
    public List<User> wrongCollectionReturn(@CacheKey(multi = true) List<Integer> ids) {
        return null;
    }

    @Override
    @Cached
    public List<User> correctIdentifier(@CacheKey(multi = true, identifier = "id", prefix = "id:") List<Integer> ids) {
        List<User> users = new ArrayList<>(2);
        for (int id : ids) {
            users.add(new User(id, "name" + id, new Date(), id, "zj" + id));
        }
        return users;
    }
}

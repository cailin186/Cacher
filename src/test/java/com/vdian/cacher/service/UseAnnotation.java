package com.vdian.cacher.service;

import com.vdian.cacher.CacheKey;
import com.vdian.cacher.Cached;
import com.vdian.cacher.Invalidate;
import com.vdian.cacher.domain.SimpleUser;
import com.vdian.cacher.enums.Expire;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jifang
 * @since 2016/12/2 下午12:06.
 */
public class UseAnnotation {

    @Cached(cache = "ehcache", prefix = "static", expire = Expire.TEN_MIN)
    public SimpleUser getFromDBOrHttp(@CacheKey(prefix = "id:") int id,
                                      @CacheKey(prefix = "address:") String address) {
        // 2. select db or http
        return new SimpleUser(id, address);
    }

    @Cached(cache = "ehcache", prefix = "static", expire = Expire.TEN_MIN)
    public List<SimpleUser> getFromDBOrHttp(@CacheKey(prefix = "id:", multi = true, identifier = "id") List<Integer> ids,
                                            @CacheKey(prefix = "address:") String address) {
        // 4. select db or http
        List<SimpleUser> users = new ArrayList<>(ids.size());
        for (int id : ids) {
            users.add(new SimpleUser(id, address));
        }

        return users;
    }

    @Invalidate(cache = "ehcache", prefix = "static")
    public void updateInfo(@CacheKey(prefix = "id:") int id,
                           @CacheKey(prefix = "address:") String address) {
        // ..
    }
}

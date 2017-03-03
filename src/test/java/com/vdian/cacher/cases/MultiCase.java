package com.vdian.cacher.cases;

import com.google.common.collect.Lists;
import com.vdian.cacher.cases.base.TestBase;
import com.vdian.cacher.domain.User;
import com.vdian.cacher.service.UserService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author jifang
 * @since 2016/11/30 下午2:19.
 */
public class MultiCase extends TestBase {

    @Autowired
    private UserService service;

    @Test
    public void testReturnMap() throws InterruptedException {
        List<Integer> ids = new LinkedList<>();
        for (int i = 1; i < 10; ++i) {
            ids.add(i);
        }
        Map<Integer, User> map = service.returnMap("name", ids, "ok");
        System.out.println(map);

        map = service.returnMap("name", ids, "ok");
        System.out.println(map);

        Thread.sleep(10000000);
    }

    @Test
    public void multiInvalidate() {
        List<Integer> ids = new LinkedList<>();
        ids.add(1);
        ids.add(2);
        service.multiInvalid("name", ids);
    }

    @Test
    public void testReturnList() {
        List<Integer> ids = Lists.newArrayList(1, 2, 3, 4);
        service.returnList(ids, "ss", new Object());
        service.returnList(ids, "ss", new Object());
    }

    @Test
    public void testUpdateList() {
        List<User> users = new ArrayList<>();
        users.add(new User(1, null, null, 1, null));
        users.add(new User(2, null, null, 1, null));
        service.batchUpdateList(users);
    }
}

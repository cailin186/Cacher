package com.vdian.cacher.cases;

import com.google.common.collect.Lists;
import com.vdian.cacher.cases.base.TestBase;
import com.vdian.cacher.domain.SimpleUser;
import com.vdian.cacher.service.UseAnnotation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author jifang
 * @since 2016/12/2 上午11:19.
 */
public class UseAnnotationTest extends TestBase {

    @Autowired
    private UseAnnotation useAnnotation;

    @Test
    public void testSingle() {
        int id = 1;
        String address = "sd";
        SimpleUser fromDBOrHttp = useAnnotation.getFromDBOrHttp(id, address);
        System.out.println(fromDBOrHttp);
        SimpleUser fromDBOrHttp1 = useAnnotation.getFromDBOrHttp(id, address);
        System.out.println(fromDBOrHttp1);
    }

    @Test
    public void testMulti() throws InterruptedException {

        System.out.println("hh");

        List<Integer> ids = Lists.newArrayList(0, 1, 2, 3);
        for (int i = 4; i < 10; ++i) {
            ids.add(i);
        }
        String address = "sd";
        List<SimpleUser> fromDBOrHttp = useAnnotation.getFromDBOrHttp(ids, address);
        System.out.println(fromDBOrHttp);

        fromDBOrHttp = useAnnotation.getFromDBOrHttp(ids, address);
        System.out.println(fromDBOrHttp);

        ids = Lists.newArrayList(1, 2, 3, 4);
        fromDBOrHttp = useAnnotation.getFromDBOrHttp(ids, address);
        System.out.println(fromDBOrHttp);
    }


    @Test
    public void testUpdate() {
        useAnnotation.updateInfo(1, "sd");
    }
}

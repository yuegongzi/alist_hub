package org.alist.hub;

import org.alist.hub.external.Aria2Client;
import org.alist.hub.util.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AListHubApplicationTests {
    @Autowired
    private Aria2Client aria2Client;
    @Test
    void contextLoads() {
        System.out.println(JsonUtils.toJson(aria2Client.active()));
    }

}

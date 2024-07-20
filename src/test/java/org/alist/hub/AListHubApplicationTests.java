package org.alist.hub;

import org.alist.hub.service.AliYunOpenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AListHubApplicationTests {
    @Autowired
    private AliYunOpenService aliYunOpenService;
    @Test
    void contextLoads() {
        aliYunOpenService.authorize("https://openapi.alipan.com/oauth/qrcode/1721454679ad3e8acba87e45b4a6f14a3580107eaa/status");
    }

}

package org.alist.hub;

import org.alist.hub.scheduler.AListHubScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AListHubApplicationTests {
    @Autowired
    private AListHubScheduler scheduler;
    @Test
    void contextLoads() {
        scheduler.sign();

    }

}

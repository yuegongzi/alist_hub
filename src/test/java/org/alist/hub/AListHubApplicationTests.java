package org.alist.hub;

import org.alist.hub.external.QuarkClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AListHubApplicationTests {
    @Autowired
    private QuarkClient quarkClient;
    @Test
    void contextLoads() {
        System.out.println(quarkClient.signInfo());
        ;

    }

}

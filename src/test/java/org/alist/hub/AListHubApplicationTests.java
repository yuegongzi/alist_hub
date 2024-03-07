package org.alist.hub;

import org.alist.hub.service.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AListHubApplicationTests {
    @Autowired
    private StorageService storageService;
    @Test
    void contextLoads() {
        storageService.removeExpire();
    }

}

package org.alist.hub.external;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bo.NoticeBO;
import org.alist.hub.client.Http;
import org.alist.hub.client.Payload;
import org.alist.hub.service.AppConfigService;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * push deer 推送
 * <a href='https://www.pushdeer.com/official.html'>官网</a>
 */
@Component
@AllArgsConstructor
@Slf4j
public class BarkClient {
    private final AppConfigService appConfigService;

    public void send(String pushKey, String title, String message) {
        Payload payload = Payload.create("https://api.day.app/" + pushKey);
        payload.addHeader("Content-Type", "application/json");
        payload.addBody("title", title);
        payload.addBody("body", message);
        new Thread(() -> Http.post(payload)).start();//不因执行失败 影响主进程运行
    }


    public void ifPresent(Consumer<? super NoticeBO> action) {
        Optional<NoticeBO> noticeBO = appConfigService.get(new NoticeBO(), NoticeBO.class);
        noticeBO.ifPresent(action);
    }

}

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
public class PushDeerClient {
    private final Http http;
    private final AppConfigService appConfigService;

    public void send(String pushKey, String title, String message) {
        Payload payload = Payload.create("https://api2.pushdeer.com/message/push");
        payload.addHeader("Content-Type", "application/json");
        payload.addParam("pushkey", pushKey);
        payload.addParam("text", title);
        payload.addParam("desp", message);
        payload.addParam("type", "markdown");
        new Thread(() -> http.post(payload)).start();//不因执行失败 影响主进程运行
    }


    public void ifPresent(Consumer<? super NoticeBO> action) {
        Optional<NoticeBO> noticeBO = appConfigService.get(new NoticeBO(), NoticeBO.class);
        noticeBO.ifPresent(action);
    }

}

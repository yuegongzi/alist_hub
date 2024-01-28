package org.alist.hub.listener;

import lombok.AllArgsConstructor;
import org.alist.hub.service.InitialService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AListListener implements ApplicationListener<ApplicationReadyEvent> {
    private final InitialService initialService;

    /**
     * 在应用启动事件中，当应用准备就绪时执行。
     *
     * @param event ApplicationReadyEvent 应用准备就绪事件
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String env = event.getApplicationContext().getEnvironment().getActiveProfiles()[0];
        if (!"dev".equals(env)) {
            initialService.execute();
        }

    }

}

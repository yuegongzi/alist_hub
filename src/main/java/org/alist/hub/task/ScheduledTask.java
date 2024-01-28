package org.alist.hub.task;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.alist.hub.api.AliYunDriveClient;
import org.alist.hub.bo.AliYunSignBO;
import org.alist.hub.service.AppConfigService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ScheduledTask {
    private final AliYunDriveClient aliYunDriveClient;
    private final AppConfigService appConfigService;

    @Scheduled(cron = "0 17 9 * * ?")
    public void sign() {
        AliYunSignBO aliYunSignBO = new AliYunSignBO();
        JsonNode jsonNode = aliYunDriveClient.sign();
        aliYunSignBO.setResult(jsonNode);
        appConfigService.saveOrUpdate(aliYunSignBO);
    }

    @Scheduled(cron = "0 36 4 * * ?")
    public void executeTask() {
        AliYunSignBO aliYunSignBO = new AliYunSignBO();
        JsonNode jsonNode = aliYunDriveClient.sign();
        aliYunSignBO.setResult(jsonNode);
        appConfigService.saveOrUpdate(aliYunSignBO);
    }
}

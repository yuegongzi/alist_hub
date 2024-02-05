package org.alist.hub.task;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.api.AliYunDriveClient;
import org.alist.hub.api.AliYunOpenClient;
import org.alist.hub.bean.Constants;
import org.alist.hub.bean.FileItem;
import org.alist.hub.bo.AliYunFolderBO;
import org.alist.hub.bo.AliYunSignBO;
import org.alist.hub.model.AppConfig;
import org.alist.hub.repository.AppConfigRepository;
import org.alist.hub.service.AListService;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.service.FileWatcherService;
import org.alist.hub.service.SearchNodeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@AllArgsConstructor
public class ScheduledTask {
    private final AliYunDriveClient aliYunDriveClient;
    private final AppConfigService appConfigService;
    private final AListService aListService;
    private final AliYunOpenClient aliYunOpenClient;
    private final SearchNodeService searchNodeService;
    private final AppConfigRepository appConfigRepository;
    private final FileWatcherService fileWatcherService;

    @Scheduled(cron = "0 17 9 * * ?")
    public void sign() {
        AliYunSignBO aliYunSignBO = new AliYunSignBO();
        JsonNode jsonNode = aliYunDriveClient.sign();
        aliYunSignBO.setResult(jsonNode);
        appConfigService.saveOrUpdate(aliYunSignBO);
    }

    @Scheduled(cron = "0 17 05 * * ?")
    public void update() {
        if (aListService.checkUpdate()) {
            aListService.update();
        }
    }

    @Scheduled(cron = "0 18 06 * * ?")
    public void build() {
        searchNodeService.build();
    }

    /**
     * 定时任务，用于删除过期文件
     */
    @Scheduled(initialDelay = 120 * 1000, fixedRate = 20 * 60 * 1000)
    public void deleteExpire() {
        Optional<AliYunFolderBO> aliYunFolderBO = appConfigService.get(new AliYunFolderBO(), AliYunFolderBO.class);
        aliYunFolderBO.ifPresent(yunFolderBO -> {
            List<FileItem> fileItemList = aliYunOpenClient.getFileList(yunFolderBO.getDriveId(), yunFolderBO.getFolderId());
            fileItemList.forEach(fileItem -> {
                try {
                    if (fileItem.getUpdatedAt().isBefore(LocalDateTime.now().minusHours(2))) {
                        boolean isSuccess = aliYunOpenClient.deleteFile(fileItem.getDriveId(), fileItem.getFileId());
                        if (isSuccess) {
                            log.info("delete file success, fileId:{}", fileItem.getFileId());
                        } else {
                            log.error("delete file failure, fileId:{}", fileItem.getFileId());
                        }
                    }
                } catch (Exception e) {
                    log.error("delete file failure", e);
                }
            });
        });
    }

    @Scheduled(initialDelay = 60 * 60 * 1000, fixedRate = 60 * 60 * 1000)
    public void copy() {
        List<AppConfig> appConfigs = appConfigRepository.findAllByGroup(Constants.WATCHER_GROUP);
        appConfigs.forEach(appConfig -> {
            fileWatcherService.merge(appConfig.getId());
        });
    }
}

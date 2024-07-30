package org.alist.hub.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.Constants;
import org.alist.hub.bean.FileItem;
import org.alist.hub.bo.AliYunDriveBO;
import org.alist.hub.bo.AliYunOpenBO;
import org.alist.hub.external.AListClient;
import org.alist.hub.external.AliYunDriveClient;
import org.alist.hub.external.AliYunOpenClient;
import org.alist.hub.external.PushDeerClient;
import org.alist.hub.external.QuarkClient;
import org.alist.hub.external.UCClient;
import org.alist.hub.model.AppConfig;
import org.alist.hub.model.Storage;
import org.alist.hub.service.AListService;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.service.FileWatcherService;
import org.alist.hub.service.SearchNodeService;
import org.alist.hub.service.StorageService;
import org.alist.hub.util.DateTimeUtils;
import org.alist.hub.util.ReplaceUtils;
import org.springframework.data.domain.Example;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@AllArgsConstructor
public class AListHubScheduler {
    private final AliYunDriveClient aliYunDriveClient;
    private final PushDeerClient pushDeerClient;
    private final AppConfigService appConfigService;
    private final AListService aListService;
    private final AliYunOpenClient aliYunOpenClient;
    private final SearchNodeService searchNodeService;
    private final FileWatcherService fileWatcherService;
    private final AListClient aListClient;
    private final UCClient ucClient;
    private final QuarkClient quarkClient;
    private final StorageService storageService;

    @Scheduled(cron = "0 17 9 * * ?")
    public void sign() {
        Optional<AliYunDriveBO> optional = appConfigService.get(new AliYunDriveBO(), AliYunDriveBO.class);
        if (optional.isPresent()) {
            AliYunDriveBO aliYunDriveBO = optional.get();
            JsonNode jsonNode = aliYunDriveClient.sign();
            aliYunDriveBO.setResult(jsonNode);
            appConfigService.saveOrUpdate(aliYunDriveBO);
            pushDeerClient.ifPresent(notice -> {
                if (notice.isSign()) {
                    Integer signCount = jsonNode.findValue("signInCount").asInt(0);
                    pushDeerClient.send(notice.getPushKey(), "阿里云盘签到成功", String.format("本月累计签到%s天", signCount));
                }
            });
        }
        String message = quarkClient.signInfo();
        pushDeerClient.ifPresent(notice -> {
            if (notice.isSign()) {
                pushDeerClient.send(notice.getPushKey(), "夸克签到: ", message);
            }
        });
    }

    @Scheduled(cron = "0 17 01 * * ?")
    public void update() {
        if (aListService.checkUpdate()) {
            aListService.update();
            pushDeerClient.ifPresent(notice -> {
                if (notice.isUpdate()) {
                    pushDeerClient.send(notice.getPushKey(), "小雅数据更新成功", String.format("更新时间: %s", DateTimeUtils.format(LocalDateTime.now())));
                }
            });
        }
    }

    @Scheduled(cron = "0 18 02 * * ?")
    public void build() {
        searchNodeService.build();
    }

    /**
     * 定时任务，用于删除过期文件
     */
    @Scheduled(initialDelay = 10 * 60 * 1000, fixedRate = 20 * 60 * 1000)
    public void deleteExpire() {
        Optional<AliYunOpenBO> aliYunOpenBO = appConfigService.get(new AliYunOpenBO(), AliYunOpenBO.class);
        aliYunOpenBO.ifPresent(openBO -> {
            List<FileItem> fileItemList = aliYunOpenClient.getFileList(openBO.getDriveId(), openBO.getFolderId());
            fileItemList.forEach(fileItem -> {
                try {
                    if (fileItem.getUpdatedAt().isBefore(LocalDateTime.now().minusHours(12))) {
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

    @Scheduled(initialDelay = 30 * 60 * 1000, fixedRate = 60 * 60 * 1000)
    public void merge() {
        AppConfig appConfig = new AppConfig();
        appConfig.setGroup(Constants.WATCHER_GROUP);
        List<AppConfig> appConfigs = appConfigService.findAll(Example.of(appConfig));
        appConfigs.forEach(a -> {
            fileWatcherService.merge(a.getId());
        });
    }

    @Scheduled(initialDelay = 10 * 60 * 1000, fixedRate = 60 * 60 * 1000)
    public void replace() {
        ReplaceUtils.replaceString(Path.of("/www/tvbox/libs/alist.min.js"), "ALIST_AUTH", aListClient.getToken());
    }

    @Scheduled(initialDelay = 30 * 60 * 1000, fixedRate = 180 * 60 * 1000)
    public void refreshCookie() {
        if (ucClient.refreshCookie()) {
            List<Storage> storages = storageService.findAllByDriver("UCShare");
            storages.forEach(storageService::flush);
        }
        ucClient.refreshCookie();

    }
}

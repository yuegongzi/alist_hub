package org.alist.hub.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.Constants;
import org.alist.hub.bean.FileSystem;
import org.alist.hub.bean.FileWatcher;
import org.alist.hub.exception.ServiceException;
import org.alist.hub.external.AListClient;
import org.alist.hub.external.Aria2Client;
import org.alist.hub.external.PushDeerClient;
import org.alist.hub.model.AppConfig;
import org.alist.hub.repository.AppConfigRepository;
import org.alist.hub.service.FileWatcherService;
import org.alist.hub.util.JsonUtils;
import org.alist.hub.util.RandomUtils;
import org.alist.hub.util.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class FileWatcherServiceImpl implements FileWatcherService {
    private final Aria2Client aria2Client;
    private final AppConfigRepository appConfigRepository;
    private final PushDeerClient pushDeerClient;
    private final AListClient aListClient;

    /**
     * 获取指定目录下的所有文件名列表
     *
     * @param directoryPath 目录路径
     * @return 文件名列表
     */
    public static List<String> listFileNamesInDirectory(String directoryPath) {
        Path path = Paths.get(directoryPath);
        // 检查目录是否存在
        if (!Files.exists(path)) {
            return new ArrayList<>(); // 新创建的目录是空的
        }
        // 尝试列出目录中的文件名称，过滤掉子目录
        try (var files = Files.list(path)) {
            return files.filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return new ArrayList<>(); // 读取文件列表失败时返回空列表
    }

    /**
     * 监视指定存储的指定路径下的文件夹变化
     *
     * @param path       文件夹路径
     * @param folderName 文件夹名称
     */
    @Override
    public void watch(String path, String folderName) {
        // 创建FileWatcher对象
        FileWatcher fileWatcher = new FileWatcher();
        fileWatcher.setFolderName(folderName);
        fileWatcher.setPath(path);
        // 创建AppConfig对象并保存
        AppConfig appConfig = new AppConfig();
        appConfig.setGroup(Constants.WATCHER_GROUP);
        appConfig.setId(RandomUtils.generateRandomString(32));
        appConfig.setValue(JsonUtils.toJson(fileWatcher));
        appConfigRepository.save(appConfig);
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                merge(appConfig.getId());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    /**
     * 文件拷贝和合并
     *
     * @param id 监听任务的ID
     */
    @Override
    public void merge(String id) {
        // 根据ID查询AppConfig对象
        Optional<AppConfig> optionalAppConfig = appConfigRepository.findById(id);
        if (optionalAppConfig.isEmpty()) {
            throw new ServiceException("监听任务不存在");
        }
        AppConfig appConfig = optionalAppConfig.get();
        // 将AppConfig对象的值转换为FileWatcher对象
        Optional<FileWatcher> optionalFileWatcher = JsonUtils.readValue(appConfig.getValue(), FileWatcher.class);
        if (optionalFileWatcher.isEmpty()) {
            throw new ServiceException("数据转换失败");
        }
        FileWatcher fileWatcher = optionalFileWatcher.get();
        List<FileSystem> fileSystems = aListClient.fs(fileWatcher.getPath());
        List<String> list = listFileNamesInDirectory("/Downloads/" + fileWatcher.getFolderName());
        boolean success = false;
        // 获取需要复制的文件列表
        for (FileSystem file : fileSystems) {
            if (!list.contains(file.getName())) {
                String rawUrl = aListClient.get(fileWatcher.getPath() + "/" + file.getName());
                if (StringUtils.hasText(rawUrl)) {
                    aria2Client.add(rawUrl, fileWatcher.getFolderName() + "/" + file.getName());
                    success = true;
                }

            }
        }
        boolean finalSuccess = success;
        pushDeerClient.ifPresent(notice -> {
            if (notice.isTransfer() && finalSuccess) {
                pushDeerClient.send(notice.getPushKey(), "转存文件成功", String.format("\n%s", fileWatcher.getFolderName()));
            }
        });
    }

}

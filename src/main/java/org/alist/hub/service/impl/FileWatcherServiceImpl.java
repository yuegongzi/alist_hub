package org.alist.hub.service.impl;

import lombok.AllArgsConstructor;
import org.alist.hub.api.AliYunDriveClient;
import org.alist.hub.api.AliYunOpenClient;
import org.alist.hub.bean.Constants;
import org.alist.hub.bean.FileInfo;
import org.alist.hub.bean.FileItem;
import org.alist.hub.bean.FileWatcher;
import org.alist.hub.bean.ShareFile;
import org.alist.hub.exception.ServiceException;
import org.alist.hub.model.AppConfig;
import org.alist.hub.model.Storage;
import org.alist.hub.repository.AppConfigRepository;
import org.alist.hub.repository.StorageRepository;
import org.alist.hub.service.FileWatcherService;
import org.alist.hub.utils.JsonUtil;
import org.alist.hub.utils.RandomUtil;
import org.alist.hub.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class FileWatcherServiceImpl implements FileWatcherService {
    private final StorageRepository storageRepository;
    private final AliYunDriveClient aliYunDriveClient;
    private final AliYunOpenClient aliYunOpenClient;
    private final AppConfigRepository appConfigRepository;

    /**
     * 监视指定存储的指定路径下的文件夹变化
     *
     * @param storageId  存储ID
     * @param path       文件夹路径
     * @param folderName 文件夹名称
     */
    @Override
    public void watch(Long storageId, String path, String folderName) {
        // 根据存储ID获取存储对象
        Optional<Storage> storage = storageRepository.findById(storageId);
        if (storage.isEmpty()) {
            throw new ServiceException("存储不存在");
        }
        Storage s = storage.get();
        // 获取根文件夹ID
        Object rootFolderId = s.getAddition().get("root_folder_id");
        String parentFileId = "root";
        if (rootFolderId != null && StringUtils.hasText(rootFolderId.toString())) {
            parentFileId = rootFolderId.toString();
        }
        // 将路径按"/"分割为数组
        String[] paths = StringUtils.split(path, "/");
        // 创建FileWatcher对象
        FileWatcher fileWatcher = new FileWatcher();
        fileWatcher.setParentFileId(parentFileId);
        // 遍历路径数组
        for (String p : paths) {
            // 调用阿里云客户端获取共享文件列表
            List<ShareFile> shareFiles = aliYunDriveClient.getShareList(s.getAddition().get("share_id").toString(), s.getAddition().get("share_pwd").toString(), fileWatcher.getParentFileId());
            // 遍历共享文件列表
            for (ShareFile file : shareFiles) {
                // 如果文件名和类型匹配，则更新FileWatcher对象的参数
                if (file.getName().equals(p) && file.getType().equals("folder")) {
                    fileWatcher.setDriveId(file.getDriveId());
                    fileWatcher.setParentFileId(file.getFileId());
                    break;
                }
            }
        }
        // 调用阿里云客户端创建文件夹
        Optional<FileInfo> fileInfo = aliYunOpenClient.createFolder(folderName, "backup", "root");
        if (fileInfo.isEmpty()) {
            throw new ServiceException("创建文件夹失败");
        }
        // 更新FileWatcher对象的参数
        fileWatcher.setFolderName(folderName);
        fileWatcher.setStorageId(storageId);
        fileWatcher.setPath(path);
        fileWatcher.setToDriveId(fileInfo.get().getDriveId());
        fileWatcher.setToFileId(fileInfo.get().getFileId());
        // 创建AppConfig对象并保存
        AppConfig appConfig = new AppConfig();
        appConfig.setGroup(Constants.WATCHER_GROUP);
        appConfig.setId(RandomUtil.generateRandomString(32));
        appConfig.setValue(JsonUtil.toJson(fileWatcher));
        appConfigRepository.save(appConfig);
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
        Optional<FileWatcher> optionalFileWatcher = JsonUtil.readValue(appConfig.getValue(), FileWatcher.class);
        if (optionalFileWatcher.isEmpty()) {
            throw new ServiceException("数据转换失败");
        }
        FileWatcher fileWatcher = optionalFileWatcher.get();
        // 根据FileWatcher对象的存储ID查询Storage对象
        Optional<Storage> storage = storageRepository.findById(fileWatcher.getStorageId());
        if (storage.isEmpty()) {
            throw new ServiceException("存储不存在");
        }
        Storage s = storage.get();
        // 获取指定驱动器中的文件列表
        List<FileItem> fileItems = aliYunOpenClient.getFileList(fileWatcher.getToDriveId(), fileWatcher.getToFileId());
        // 获取指定分享列表中的文件列表
        List<ShareFile> shareFiles = aliYunDriveClient.getShareList(s.getAddition().get("share_id").toString(), s.getAddition().get("share_pwd").toString(), fileWatcher.getParentFileId());
        // 获取需要复制的文件列表
        List<String> array = new ArrayList<>();
        for (ShareFile file : shareFiles) {
            // 在文件列表中查找名称匹配的文件
            Optional<String> matchingFileId = fileItems.stream()
                    .filter(item -> item.getName().equals(file.getName()))
                    .map(FileItem::getFileId)
                    .filter(fileId -> !fileId.isEmpty()) // 如果需要过滤掉空的fileId
                    .findFirst();

            if (matchingFileId.isEmpty()) {
                array.add(file.getFileId());
            }
        }
        // 复制文件
        aliYunDriveClient.copy(fileWatcher, s.getAddition().get("share_id").toString(), s.getAddition().get("share_pwd").toString(), array);
    }
}

package org.alist.hub.service.impl;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.Constants;
import org.alist.hub.bo.AliYunDriveBO;
import org.alist.hub.bo.AliYunOpenBO;
import org.alist.hub.drive.DefaultDrive;
import org.alist.hub.drive.PikParkDrive;
import org.alist.hub.exception.ServiceException;
import org.alist.hub.external.AListClient;
import org.alist.hub.model.Meta;
import org.alist.hub.model.Storage;
import org.alist.hub.repository.StorageRepository;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.service.MetaService;
import org.alist.hub.service.StorageService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 存储服务实现类
 */
@Service
@Slf4j
public class StorageServiceImpl extends GenericServiceImpl<Storage, Long> implements StorageService {
    private final StorageRepository repository;
    @Resource
    private AppConfigService appConfigService;
    @Resource
    private MetaService metaService;
    @Resource
    private AListClient aListClient;
    @Resource
    private PikParkDrive pikParkDrive;
    @Resource
    private DefaultDrive defaultDrive;

    public StorageServiceImpl(StorageRepository repository) {
        super(repository);
        this.repository = repository;
    }

    /**
     * 设置阿里云存储
     */
    private void setAli() {
        // 从数据库中获取所有驱动为"AliyundriveShare2Open"的存储
        List<Storage> storages = repository.findAllByDriver("AliyundriveShare2Open");
        // 从应用配置服务中获取阿里云盘开放接口配置
        Optional<AliYunOpenBO> aliYunOpenBO = appConfigService.get(new AliYunOpenBO(), AliYunOpenBO.class);
        // 从应用配置服务中获取阿里云盘配置
        Optional<AliYunDriveBO> aliYunDriveBO = appConfigService.get(new AliYunDriveBO(), AliYunDriveBO.class);
        // 如果阿里云盘配置为空，则抛出服务异常
        if (aliYunOpenBO.isEmpty() || aliYunDriveBO.isEmpty()) {
            throw new ServiceException("阿里云盘配置未填写");
        }

        // 遍历所有存储实例
        for (Storage storage : storages) {
            // 创建存储附加信息的映射
            Map<String, Object> addition = new HashMap<>(storage.getAddition());
            // 设置刷新令牌
            addition.put("RefreshToken", aliYunDriveBO.get().getRefreshToken());
            // 设置阿里云盘开放接口的刷新令牌
            addition.put("RefreshTokenOpen", aliYunOpenBO.get().getRefreshToken());
            // 设置临时传输文件夹ID
            addition.put("TempTransferFolderID", aliYunOpenBO.get().getFolderId());
            // 设置存储类型
            addition.put("rorb", "r");
            // 更新存储实例的附加信息
            storage.setAddition(addition);
            // 保存更新后的存储实例到数据库
            saveAndFlush(storage);
        }
    }

    /**
     * 重置存储配置
     */
    @Override
    @Transactional
    public void resetStorage() {
        // 删除数据库中存储ID小于4的存储实例和元数据
        repository.deleteByIdLessThan(4L);
        // 更新存储驱动
        repository.updateDriver("AliyundriveShare", "AliyundriveShare2Open");
        // 设置阿里云分享存储
        setAli();
        // 设置我的阿里云存储
        setMyAli();
        // 设置PikPak存储
        pikParkDrive.initialize();
        // 设置默认存储
        defaultDrive.initialize();
    }

    /**
     * 删除过期的存储和元数据
     */
    @Transactional
    @Override
    public void removeExpire() {
        // 删除数据库中存储ID小于且未禁用的存储实例和元数据
        repository.deleteByIdLessThanAndDisabled(Constants.MY_ALI_ID, false);
        // 更新数据库中存储ID小于4的元数据的隐藏属性为1
        metaService.updateHideLessThan(4, "1");
        // 加载数据库迁移脚本
        ClassPathResource classPathResource = new ClassPathResource("db/migration/readme.txt");
        try {
            // 读取迁移脚本内容
            byte[] bytes = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            String content = new String(bytes, StandardCharsets.UTF_8);
            // 从数据库中获取ID为4的元数据实例
            Optional<Meta> optional = metaService.findById(4);
            // 如果获取到元数据实例，则将迁移脚本内容设置为元数据的读取文档
            optional.map(meta -> {
                meta.setReadme(content);
                metaService.save(meta);
                return null;
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 设置我的阿里云存储
     */
    public void setMyAli() {
        // 从数据库中根据存储ID获取我的阿里云存储实例
        Optional<Storage> optional = findById(Constants.MY_ALI_ID);
        // 从应用配置服务中获取阿里云盘配置
        Optional<AliYunOpenBO> aliYunOpenBO = appConfigService.get(new AliYunOpenBO(), AliYunOpenBO.class);

        // 如果阿里云盘配置为空，则抛出服务异常
        if (aliYunOpenBO.isEmpty()) {
            throw new ServiceException("阿里云盘配置未填写");
        }

        // 如果获取不到我的阿里云存储实例，则创建新的存储实例
        if (optional.isEmpty()) {
            Storage storage = new Storage();
            storage.build();
            storage.setId(Constants.MY_ALI_ID);
            storage.setMountPath("/\uD83D\uDCC0我的阿里云盘");
            storage.setDriver("AliyundriveOpen");
            storage.setDisabled(false);
            // 创建存储附加信息的映射
            Map<String, Object> addition = new HashMap<>();
            addition.put("root_folder_id", "root");
            addition.put("refresh_token", aliYunOpenBO.get().getRefreshToken());
            addition.put("order_by", "name");
            addition.put("order_direction", "ASC");
            addition.put("oauth_token_url", Constants.API_DOMAIN + "/alist/ali_open/token");
            addition.put("client_id", "");
            addition.put("client_secret", "");
            addition.put("remove_way", "");
            addition.put("internal_upload", false);
            addition.put("AccessToken", aliYunOpenBO.get().getAccessToken());
            storage.setAddition(addition);
            // 保存新的存储实例到数据库
            save(storage);
        }
    }

    /**
     * 刷新存储信息。
     *
     * @param storage 需要刷新的存储对象
     */
    @Override
    public void flush(Storage storage) {
        // 获取阿里云开放平台基本信息
        Optional<AliYunOpenBO> aliYunOpenBO = appConfigService.get(new AliYunOpenBO(), AliYunOpenBO.class);
        switch (storage.getDriver()) {
            case "AliyundriveShare2Open":
                // 获取阿里云盘相关配置信息
                Optional<AliYunDriveBO> aliYunDriveBO = appConfigService.get(new AliYunDriveBO(), AliYunDriveBO.class);
                if (aliYunOpenBO.isPresent() && aliYunDriveBO.isPresent()) {
                    // 更新存储附加信息，添加阿里云盘的RefreshToken、开放平台RefreshToken及临时传输文件夹ID
                    Map<String, Object> addition = new HashMap<>(storage.getAddition());
                    addition.put("RefreshToken", aliYunDriveBO.get().getRefreshToken());
                    addition.put("RefreshTokenOpen", aliYunOpenBO.get().getRefreshToken());
                    addition.put("TempTransferFolderID", aliYunOpenBO.get().getFolderId());
                    addition.put("rorb", "r");
                    storage.setAddition(addition);
                }
                break;
            case "AliyundriveOpen":
                if (aliYunOpenBO.isPresent()) {
                    // 更新存储附加信息，添加阿里云开放平台的AccessToken和RefreshToken
                    Map<String, Object> addition = new HashMap<>(storage.getAddition());
                    addition.put("AccessToken", aliYunOpenBO.get().getAccessToken());
                    addition.put("refresh_token", aliYunOpenBO.get().getRefreshToken());
                    storage.setAddition(addition);
                }
                break;
            default:
                Map<String, Object> config = appConfigService.get(storage.getDriver());
                Map<String, Object> addition = new HashMap<>(storage.getAddition());
                addition.putAll(config);
                storage.setAddition(addition);

        }
        aListClient.addOrUpdate(storage);
    }

    @Override
    public List<Storage> findAllByDriver(String driver) {
        return this.repository.findAllByDriver(driver);
    }

    @Override
    public void deleteByIdLessThanAndDisabled(Long id, boolean disabled) {
        this.repository.deleteByIdLessThanAndDisabled(id, disabled);
    }

    @Override
    public void deleteByIdLessThan(Long id) {
        this.repository.deleteByIdLessThan(id);
    }

    @Override
    public void updateDriver(String oldDriver, String driver) {
        this.repository.updateDriver(oldDriver, driver);
    }

    @Override
    public List<Storage> findAllByIdGreaterThan(Long id) {
        return this.repository.findAllByIdGreaterThan(id);
    }
}

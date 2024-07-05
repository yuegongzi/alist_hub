package org.alist.hub.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.Constants;
import org.alist.hub.bo.AliYunDriveBO;
import org.alist.hub.bo.AliYunFolderBO;
import org.alist.hub.bo.AliYunOpenBO;
import org.alist.hub.bo.PikPakBo;
import org.alist.hub.bo.QuarkBO;
import org.alist.hub.exception.ServiceException;
import org.alist.hub.external.AListClient;
import org.alist.hub.model.Meta;
import org.alist.hub.model.Storage;
import org.alist.hub.repository.MetaRepository;
import org.alist.hub.repository.StorageRepository;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.service.StorageService;
import org.alist.hub.util.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 存储服务实现类
 */
@Service
@AllArgsConstructor
@Slf4j
public class StorageServiceImpl implements StorageService {
    private final StorageRepository storageRepository;
    private final AppConfigService appConfigService;
    private final MetaRepository metaRepository;
    private final AListClient aListClient;

    /**
     * 设置阿里云存储
     */
    private void setAli() {
        // 从数据库中获取所有驱动为"AliyundriveShare2Open"的存储
        List<Storage> storages = storageRepository.findAllByDriver("AliyundriveShare2Open");
        // 从应用配置服务中获取阿里云盘开放接口配置
        Optional<AliYunOpenBO> aliYunOpenBO = appConfigService.get(new AliYunOpenBO(), AliYunOpenBO.class);
        // 从应用配置服务中获取阿里云盘配置
        Optional<AliYunDriveBO> aliYunDriveBO = appConfigService.get(new AliYunDriveBO(), AliYunDriveBO.class);
        // 从应用配置服务中获取阿里云盘文件夹配置
        Optional<AliYunFolderBO> aliYunFolderBO = appConfigService.get(new AliYunFolderBO(), AliYunFolderBO.class);

        // 如果阿里云盘配置为空，则抛出服务异常
        if (aliYunOpenBO.isEmpty() || aliYunDriveBO.isEmpty() || aliYunFolderBO.isEmpty()) {
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
            addition.put("TempTransferFolderID", aliYunFolderBO.get().getFolderId());
            // 设置存储类型
            addition.put("rorb", "r");
            // 更新存储实例的附加信息
            storage.setAddition(addition);
            // 保存更新后的存储实例到数据库
            storageRepository.saveAndFlush(storage);
        }
    }

    /**
     * 重置存储配置
     */
    @Override
    @Transactional
    public void resetStorage() {
        // 删除数据库中存储ID小于4的存储实例和元数据
        storageRepository.deleteByIdLessThan(4L);
        // 更新存储驱动
        storageRepository.updateDriver("AliyundriveShare", "AliyundriveShare2Open");
        setQuark();
        // 设置阿里云分享存储
        setAli();
        // 设置我的阿里云存储
        setMyAli();
        // 设置PikPak存储
        setPikPak();
    }

    /**
     * 删除过期的存储和元数据
     */
    @Transactional
    @Override
    public void removeExpire() {
        // 删除数据库中存储ID小于且未禁用的存储实例和元数据
        storageRepository.deleteByIdLessThanAndDisabled(Constants.MY_ALI_ID, false);
        // 更新数据库中存储ID小于4的元数据的隐藏属性为1
        metaRepository.updateHideLessThan(4, "1");
        // 加载数据库迁移脚本
        ClassPathResource classPathResource = new ClassPathResource("db/migration/readme.txt");
        try {
            // 读取迁移脚本内容
            byte[] bytes = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            String content = new String(bytes, StandardCharsets.UTF_8);
            // 从数据库中获取ID为4的元数据实例
            Optional<Meta> optional = metaRepository.findById(4);
            // 如果获取到元数据实例，则将迁移脚本内容设置为元数据的读取文档
            optional.map(meta -> {
                meta.setReadme(content);
                metaRepository.save(meta);
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
        Optional<Storage> optional = storageRepository.findById(Constants.MY_ALI_ID);
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
            storageRepository.save(storage);
        }
    }

    /**
     * 设置PikPak存储
     */
    @Transactional
    public void setPikPak() {
        try {
            // 读取PikPak分享列表文件的所有行
            List<String> strings = Files.readAllLines(Path.of("/var/lib/data/pikpakshare_list.txt"));
            // 创建存储实例列表
            List<Storage> list = new ArrayList<>();
            // 从应用配置服务中获取PikPak配置
            Optional<PikPakBo> pikPakBo = appConfigService.get(new PikPakBo(), PikPakBo.class);

            // 遍历分享列表文件的每一行
            for (int i = 0; i < strings.size(); i++) {
                String[] arr = StringUtils.split(strings.get(i), " ");
                Storage storage = new Storage();
                storage.setDriver("PikPakShare");
                storage.setId(9001L + i);
                storage.build();
                storage.setDisabled(false);
                storage.setMountPath("/\uD83D\uDD78\uFE0F我的PikPak分享/" + arr[0]);
                // 创建存储附加信息的映射
                Map<String, Object> addition = new HashMap<>();
                addition.put("share_id", arr[1]);
                addition.put("root_folder_id", arr[2]);
                addition.put("share_pwd", "");
                // 如果获取到PikPak配置，则将用户名和密码设置到存储附加信息中
                if (pikPakBo.isPresent()) {
                    addition.put("username", pikPakBo.get().getUsername());
                    addition.put("password", pikPakBo.get().getPassword());
                }
                storage.setAddition(addition);
                list.add(storage);
            }
            // 保存所有新的存储实例到数据库
            storageRepository.saveAll(list);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 设置Quark存储的cookie信息。
     * 该方法遍历所有驱动为QuarkShare的存储对象，如果找到相应的QuarkBO配置，
     * 则将cookie信息更新到这些存储对象的附加信息中，并保存更新。
     *
     * @Transactional 注解确保该方法的操作作为一个事务执行，确保数据的一致性。
     */
    @Transactional
    public void setQuark() {
        // 查找所有驱动为QuarkShare的存储信息
        List<Storage> storages = storageRepository.findAllByDriver("QuarkShare");
        // 尝试获取QuarkBO配置
        Optional<QuarkBO> quarkBO = appConfigService.get(new QuarkBO(), QuarkBO.class);
        // 如果QuarkBO配置不存在，则直接返回，不进行后续操作
        if (quarkBO.isEmpty()) {
            return;
        }
        // 遍历所有找到的存储信息
        for (Storage storage : storages) {
            // 创建一个HashMap来更新存储的附加信息，基于当前存储的附加信息
            Map<String, Object> addition = new HashMap<>(storage.getAddition());
            // 将QuarkBO中的cookie信息添加到附加信息中
            addition.put("cookie", quarkBO.get().getCookie());
            // 更新存储的附加信息，并立即保存
            storage.setAddition(addition);
            storageRepository.saveAndFlush(storage);
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
            case "PikPakShare":
                // 获取PikPakBo配置信息
                Optional<PikPakBo> pikPakBo = appConfigService.get(new PikPakBo(), PikPakBo.class);
                if (pikPakBo.isPresent()) {
                    // 更新存储附加信息，添加PikPakBo中的用户名和密码
                    Map<String, Object> addition = new HashMap<>(storage.getAddition());
                    addition.put("username", pikPakBo.get().getUsername());
                    addition.put("password", pikPakBo.get().getPassword());
                    storage.setAddition(addition);
                    // 将更新后的存储信息保存或更新到aListClient
                    aListClient.addOrUpdate(storage);
                }
                break;
            case "AliyundriveShare2Open":
                // 获取阿里云盘相关配置信息
                Optional<AliYunDriveBO> aliYunDriveBO = appConfigService.get(new AliYunDriveBO(), AliYunDriveBO.class);
                Optional<AliYunFolderBO> aliYunFolderBO = appConfigService.get(new AliYunFolderBO(), AliYunFolderBO.class);
                if (aliYunOpenBO.isPresent() && aliYunDriveBO.isPresent() && aliYunFolderBO.isPresent()) {
                    // 更新存储附加信息，添加阿里云盘的RefreshToken、开放平台RefreshToken及临时传输文件夹ID
                    Map<String, Object> addition = new HashMap<>(storage.getAddition());
                    addition.put("RefreshToken", aliYunDriveBO.get().getRefreshToken());
                    addition.put("RefreshTokenOpen", aliYunOpenBO.get().getRefreshToken());
                    addition.put("TempTransferFolderID", aliYunFolderBO.get().getFolderId());
                    addition.put("rorb", "r");
                    storage.setAddition(addition);
                    // 将更新后的存储信息保存或更新到aListClient
                    aListClient.addOrUpdate(storage);
                }
                break;
            case "AliyundriveOpen":
                if (aliYunOpenBO.isPresent()) {
                    // 更新存储附加信息，添加阿里云开放平台的AccessToken和RefreshToken
                    Map<String, Object> addition = new HashMap<>(storage.getAddition());
                    addition.put("AccessToken", aliYunOpenBO.get().getAccessToken());
                    addition.put("refresh_token", aliYunOpenBO.get().getRefreshToken());
                    storage.setAddition(addition);
                    // 将更新后的存储信息保存或更新到aListClient
                    aListClient.addOrUpdate(storage);
                }
                break;
            case "QuarkShare":
                Optional<QuarkBO> quarkBO = appConfigService.get(new QuarkBO(), QuarkBO.class);
                if (quarkBO.isPresent()) {
                    Map<String, Object> addition = new HashMap<>(storage.getAddition());
                    addition.put("cookie", quarkBO.get().getCookie());
                    storage.setAddition(addition);
                    // 将更新后的存储信息保存或更新到aListClient
                    aListClient.addOrUpdate(storage);
                }
                break;
            default:
                // 若未匹配到特定驱动类型，直接将存储信息保存或更新到aListClient
                aListClient.addOrUpdate(storage);
        }
    }

}

package org.alist.hub.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.Constants;
import org.alist.hub.bo.AliYunDriveBO;
import org.alist.hub.bo.AliYunFolderBO;
import org.alist.hub.bo.AliYunOpenBO;
import org.alist.hub.bo.PikPakBo;
import org.alist.hub.exception.ServiceException;
import org.alist.hub.model.Meta;
import org.alist.hub.model.Storage;
import org.alist.hub.repository.MetaRepository;
import org.alist.hub.repository.StorageRepository;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.service.StorageService;
import org.alist.hub.utils.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class StorageServiceImpl implements StorageService {
    private final StorageRepository storageRepository;
    private final AppConfigService appConfigService;
    private final MetaRepository metaRepository;

    @Override
    public void setAliAddition(Storage... storages) {
        Optional<AliYunOpenBO> aliYunOpenBO = appConfigService.get(new AliYunOpenBO(), AliYunOpenBO.class);
        Optional<AliYunDriveBO> aliYunDriveBO = appConfigService.get(new AliYunDriveBO(), AliYunDriveBO.class);
        Optional<AliYunFolderBO> aliYunFolderBO = appConfigService.get(new AliYunFolderBO(), AliYunFolderBO.class);
        if (aliYunOpenBO.isEmpty() || aliYunDriveBO.isEmpty() || aliYunFolderBO.isEmpty()) {
            throw new ServiceException("阿里云盘配置未填写");
        }
        List<Storage> list = new ArrayList<>();
        Arrays.stream(storages).toList().forEach(storage -> {
            Map<String, Object> addition = storage.getAddition();
            addition.put("RefreshToken", aliYunDriveBO.get().getRefreshToken());
            addition.put("RefreshTokenOpen", aliYunOpenBO.get().getRefreshToken());
            addition.put("TempTransferFolderID", aliYunFolderBO.get().getFolderId());
            addition.put("oauth_token_url", Constants.API_DOMAIN + "/alist/ali_open/token");
            storage.setAddition(addition);
            list.add(storage);
        });
        storageRepository.saveAll(list);
    }

    @Override
    public void setPikPakAddition(Storage... storages) {
        Optional<PikPakBo> pikPakBo = appConfigService.get(new PikPakBo(), PikPakBo.class);
        if (pikPakBo.isPresent()) {
            List<Storage> list = new ArrayList<>();
            Arrays.stream(storages).toList().forEach(storage -> {
                Map<String, Object> addition = storage.getAddition();
                addition.put("username", pikPakBo.get().getUsername());
                addition.put("password", pikPakBo.get().getPassword());
                storage.setAddition(addition);
                storage.setAddition(addition);
                list.add(storage);
            });
            storageRepository.saveAll(list);
        }

    }

    @Override
    @Transactional
    public void updateAliYunDrive() {
        storageRepository.updateDriver("AliyundriveShare", "AliyundriveShare2Open");
        List<Storage> list = storageRepository.findAllByDriver("AliyundriveShare2Open");
        setAliAddition(list.toArray(new Storage[0]));
    }


    @Transactional
    @Override
    public void removeAll() {
        storageRepository.deleteByIdLessThan(10000L);//删除旧的存储
        metaRepository.deleteByIdGreaterThan(4);//删除旧元数据
        metaRepository.updateHideLessThan(4, "1");
        ClassPathResource classPathResource = new ClassPathResource("db/migration/readme.txt");
        try {
            byte[] bytes = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            String content = new String(bytes, StandardCharsets.UTF_8);
            Optional<Meta> optional = metaRepository.findById(4);
            optional.map(meta -> {
                meta.setReadme(content);
                metaRepository.save(meta);
                return null;
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    @Override
    public boolean getMyAli() {
        long id = 10000;
        Optional<Storage> optional = storageRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get().isDisabled();
        }
        Optional<AliYunOpenBO> aliYunOpenBO = appConfigService.get(new AliYunOpenBO(), AliYunOpenBO.class);
        if (aliYunOpenBO.isEmpty()) {
            throw new ServiceException("阿里云盘配置未填写");
        }
        Storage storage = new Storage();
        storage.build();
        storage.setId(id);
        storage.setMountPath("/\uD83D\uDCC0我的阿里云盘");
        storage.setDriver("AliyundriveOpen");
        storage.setDisabled(true);
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
        storageRepository.save(storage);
        return true;
    }

    @Override
    public void updateMyAli(boolean disabled) {
        long id = 10000;
        Optional<Storage> optional = storageRepository.findById(id);
        if (optional.isPresent()) {
            Storage storage = optional.get();
            storage.setDisabled(disabled);
            storageRepository.save(storage);
        }
    }

    @Override
    public void updatePikPak() {
        Optional<PikPakBo> pikPakBo = appConfigService.get(new PikPakBo(), PikPakBo.class);
        if (pikPakBo.isPresent()) {
            try {
                List<String> strings = Files.readAllLines(Path.of("/var/lib/data/pikpakshare_list.txt"));
                List<Storage> list = storageRepository.findAllByDriver("PikPakShare");
                for (int i = 0; i < strings.size(); i++) {
                    String[] arr = StringUtils.split(strings.get(i), " ");
                    Storage storage = new Storage();
                    storage.setDriver("PikPakShare");
                    storage.setId(9001L + i);
                    storage.build();
                    storage.setDisabled(false);
                    storage.setMountPath("/\uD83D\uDD78\uFE0F我的PikPak分享/" + arr[0]);
                    Map<String, Object> addition = new HashMap<>();
                    addition.put("share_id", arr[1]);
                    addition.put("root_folder_id", arr[2]);
                    addition.put("share_pwd", "");
                    storage.setAddition(addition);
                    list.add(storage);
                }
                setPikPakAddition(list.toArray(new Storage[0]));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}

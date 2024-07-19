package org.alist.hub.drive;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.model.Storage;
import org.alist.hub.repository.StorageRepository;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.util.StringUtils;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
@Slf4j
public class PikParkDrive implements Drive {
    private final AppConfigService appConfigService;
    private final StorageRepository storageRepository;

    @Override
    public void initialize() {
        try {
            Map<String, Object> pikPakConfig = appConfigService.get("PikPakShare");
            // 读取PikPak分享列表文件的所有行
            List<String> strings = Files.readAllLines(Path.of("/var/lib/data/pikpakshare_list.txt"));
            // 创建存储实例列表
            List<Storage> list = new ArrayList<>();
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
                if (!pikPakConfig.isEmpty()) {
                    addition.putAll(pikPakConfig);
                }
                storage.setAddition(addition);
                list.add(storage);
            }
            // 保存所有新的存储实例到数据库
            storageRepository.saveAll(list);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

}

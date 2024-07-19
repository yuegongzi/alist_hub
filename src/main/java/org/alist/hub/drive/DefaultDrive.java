package org.alist.hub.drive;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.model.Storage;
import org.alist.hub.repository.StorageRepository;
import org.alist.hub.service.AppConfigService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@AllArgsConstructor
public class DefaultDrive implements Drive {
    private StorageRepository storageRepository;
    private AppConfigService appConfigService;

    @Override
    public void initialize() {
        try {
            // 查找所有驱动为QuarkShare的存储信息
            Iterable<Storage> storages = storageRepository.findAll();
            for (Storage storage : storages) {
                switch (storage.getDriver()) {
                    case "115 Share":
                    case "QuarkShare":
                    case "UCShare":
                        Map<String, Object> config = appConfigService.get(storage.getDriver());
                        Map<String, Object> addition = new HashMap<>(storage.getAddition());
                        addition.putAll(config);
                        storage.setAddition(addition);
                        storageRepository.save(storage);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}

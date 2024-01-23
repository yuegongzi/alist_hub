package org.alist.hub.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.Constants;
import org.alist.hub.bo.Persistent;
import org.alist.hub.model.AppConfig;
import org.alist.hub.repository.AppConfigRepository;
import org.alist.hub.service.AppConfigService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class AppConfigServiceImpl implements AppConfigService {
    private final AppConfigRepository appConfigRepository;


    @Override
    public boolean isInitialized() {
        Optional<AppConfig> appConfig = appConfigRepository.findById(Constants.APP_INIT);
        return appConfig.isPresent();
    }

    @Override
    public void initialize() {
        AppConfig appConfig = new AppConfig();
        appConfig.setValue("true");
        appConfig.setId(Constants.APP_INIT);
        appConfig.setGroup(Constants.APP_GROUP);
        appConfigRepository.save(appConfig);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void saveOrUpdate(Persistent persistent) throws IOException {
        AppConfig appConfig = new AppConfig();
        appConfig.setId(persistent.getId());
        appConfig.setValue(persistent.getValue());
        appConfig.setGroup(Constants.ALIST_GROUP);
        AppConfig file = new AppConfig();
        file.setId("/data/" + persistent.getId());
        file.setValue(persistent.getFileValue());
        file.setGroup(Constants.FILE_GROUP);
        Path path = Path.of("/data/" + persistent.getId());
        Files.writeString(path, persistent.getFileValue(), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        appConfigRepository.save(file);
        appConfigRepository.save(appConfig);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void remove(Persistent persistent) throws IOException {
        appConfigRepository.deleteById(persistent.getId());
        appConfigRepository.deleteById("/data/" + persistent.getId());
        Files.delete(Path.of("/data/" + persistent.getId()));
    }
}

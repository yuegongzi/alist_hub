package org.alist.hub.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.Constants;
import org.alist.hub.bo.Persistent;
import org.alist.hub.model.AppConfig;
import org.alist.hub.repository.AppConfigRepository;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.utils.JsonUtil;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class AppConfigServiceImpl implements AppConfigService {
    private final AppConfigRepository appConfigRepository;


    @Override
    public boolean isInitialized() {
        Optional<AppConfig> appConfig = appConfigRepository.findById(Constants.APP_INIT);
        return appConfig.map(a -> a.getValue().equals("true")).orElse(false);
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
    public void saveOrUpdate(Persistent persistent) {
        AppConfig appConfig = new AppConfig();
        appConfig.setId(persistent.getId());
        appConfig.setValue(persistent.getValue());
        appConfig.setGroup(Constants.ALIST_GROUP);
        appConfigRepository.save(appConfig);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void remove(Persistent persistent) {
        appConfigRepository.deleteById(persistent.getId());
    }

    @Override
    public <T> Optional<T> get(Persistent persistent, Class<T> clazz) {
        Optional<AppConfig> appConfig = appConfigRepository.findById(persistent.getId());
        if (appConfig.isPresent()) {
            return JsonUtil.readValue(appConfig.get().getValue(), clazz);
        }
        return Optional.empty();
    }
}

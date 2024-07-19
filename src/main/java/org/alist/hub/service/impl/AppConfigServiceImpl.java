package org.alist.hub.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.Constants;
import org.alist.hub.bo.Persistent;
import org.alist.hub.model.AppConfig;
import org.alist.hub.repository.AppConfigRepository;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.util.JsonUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class AppConfigServiceImpl extends GenericServiceImpl<AppConfig, String> implements AppConfigService {

    public AppConfigServiceImpl(AppConfigRepository repository) {
        super(repository);
    }

    @Override
    public boolean isInitialized() {
        Optional<AppConfig> appConfig = findById(Constants.APP_INIT);
        return appConfig.map(a -> a.getValue().equals("true")).orElse(false);
    }

    @Override
    public void initialize() {
        AppConfig appConfig = new AppConfig();
        appConfig.setValue("true");
        appConfig.setId(Constants.APP_INIT);
        appConfig.setGroup(Constants.APP_GROUP);
        save(appConfig);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void saveOrUpdate(Persistent persistent) {
        AppConfig appConfig = new AppConfig();
        Optional<AppConfig> store = findById(persistent.getId());
        if (store.isPresent()) {
            JsonNode mainNode = JsonUtils.readTree(store.get().getValue());
            JsonNode updateNode = JsonUtils.readTree(persistent.getValue());
            JsonNode mergeNode = JsonUtils.mergeJsonNodes(mainNode, updateNode);
            appConfig.setValue(JsonUtils.toJson(mergeNode));
        } else {
            appConfig.setValue(persistent.getValue());
        }
        appConfig.setId(persistent.getId());
        appConfig.setGroup(Constants.ALIST_GROUP);
        save(appConfig);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void remove(Persistent persistent) {
        deleteById(persistent.getId());
    }

    @Override
    public <T> Optional<T> get(Persistent persistent, Class<T> clazz) {
        Optional<AppConfig> appConfig = findById(persistent.getId());
        if (appConfig.isPresent()) {
            return JsonUtils.readValue(appConfig.get().getValue(), clazz);
        }
        return Optional.empty();
    }

    @Override
    public Map<String, Object> get(String id) {
        Optional<AppConfig> appConfig = findById(id);
        if (appConfig.isPresent()) {
            return JsonUtils.toMap(appConfig.get().getValue());
        }
        return new HashMap<>();
    }
}

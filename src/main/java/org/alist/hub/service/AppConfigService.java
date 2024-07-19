package org.alist.hub.service;

import org.alist.hub.bo.Persistent;
import org.alist.hub.model.AppConfig;

import java.util.Map;
import java.util.Optional;

public interface AppConfigService extends GenericService<AppConfig, String> {

    boolean isInitialized();

    void initialize();

    void saveOrUpdate(Persistent persistent);

    void remove(Persistent persistent);

    <T> Optional<T> get(Persistent persistent, Class<T> clazz);

    Map<String, Object> get(String id);
}

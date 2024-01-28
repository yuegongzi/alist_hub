package org.alist.hub.service;

import org.alist.hub.bo.Persistent;

import java.util.Optional;

public interface AppConfigService {

    boolean isInitialized();

    void initialize();

    void saveOrUpdate(Persistent persistent);

    void remove(Persistent persistent);

    <T> Optional<T> get(Persistent persistent, Class<T> clazz);
}

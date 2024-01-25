package org.alist.hub.service;

import org.alist.hub.bo.Persistent;

import java.io.IOException;
import java.util.Optional;

public interface AppConfigService {

    boolean isInitialized();

    void initialize();

    void saveOrUpdate(Persistent persistent) throws IOException;

    void remove(Persistent persistent) throws IOException;

    <T> Optional<T> get(Persistent persistent, Class<T> clazz);
}

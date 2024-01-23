package org.alist.hub.service;

import org.alist.hub.bo.Persistent;

import java.io.IOException;

public interface AppConfigService {

    boolean isInitialized();

    void initialize();

    void saveOrUpdate(Persistent persistent) throws IOException;

    void remove(Persistent persistent) throws IOException;
}

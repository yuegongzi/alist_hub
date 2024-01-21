package org.alist.hub.service;

import org.alist.hub.dto.SettingDTO;

public interface AppConfigService {
    boolean saveOrUpdate(SettingDTO settingDTO);

    boolean isInitialized();

    void initialize();
}

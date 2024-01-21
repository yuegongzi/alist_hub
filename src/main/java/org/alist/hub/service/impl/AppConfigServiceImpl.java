package org.alist.hub.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.alist.hub.bean.Constants;
import org.alist.hub.dto.SettingDTO;
import org.alist.hub.model.AppConfig;
import org.alist.hub.repository.AppConfigRepository;
import org.alist.hub.service.AppConfigService;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AppConfigServiceImpl implements AppConfigService {
    private final AppConfigRepository appConfigRepository;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public boolean saveOrUpdate(SettingDTO settingDTO) {
        execute(Constants.my_token, settingDTO.getRefresh_token());
        execute(Constants.my_open_token, settingDTO.getOpen_token());
        execute(Constants.temp_transfer_folder_id, settingDTO.getTransfer_folder_id());
        return true;
    }

    private void execute(String label, String value) {
        AppConfig config = appConfigRepository.findByLabelAndGroup(label, Constants.ALIST_GROUP);
        if (config == null) {
            config = new AppConfig();
            config.setGroup(Constants.ALIST_GROUP);
            config.setLabel(label);
            config.setValue(value);
            config.setSafe(true);
        } else {
            config.setValue(value);
        }
        appConfigRepository.save(config);
    }

    @Override
    public boolean isInitialized() {
        AppConfig appConfig = appConfigRepository.findByLabelAndGroup(Constants.APP_INIT, Constants.APP_GROUP);
        return appConfig != null && appConfig.getValue().equals("true");
    }

    @Override
    public void initialize() {
        AppConfig appConfig = new AppConfig();
        appConfig.setValue("true");
        appConfig.setLabel(Constants.APP_INIT);
        appConfig.setGroup(Constants.APP_GROUP);
        appConfig.setSafe(true);
        appConfigRepository.save(appConfig);
    }
}

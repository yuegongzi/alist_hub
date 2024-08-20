package org.alist.hub.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.Constants;
import org.alist.hub.bo.AliYunDriveBO;
import org.alist.hub.bo.Aria2BO;
import org.alist.hub.configure.HubProperties;
import org.alist.hub.dto.AccountDTO;
import org.alist.hub.dto.SecurityDTO;
import org.alist.hub.exception.ServiceException;
import org.alist.hub.external.AListClient;
import org.alist.hub.model.AppConfig;
import org.alist.hub.model.Storage;
import org.alist.hub.model.User;
import org.alist.hub.service.AListService;
import org.alist.hub.service.AliYunDriveService;
import org.alist.hub.service.AliYunOpenService;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.service.StorageService;
import org.alist.hub.service.UserService;
import org.alist.hub.util.JsonUtils;
import org.alist.hub.util.RandomUtils;
import org.alist.hub.util.StringUtils;
import org.alist.hub.vo.AccountVO;
import org.alist.hub.vo.ConfigVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/setting")
@AllArgsConstructor
@Slf4j
public class SettingController {
    private final UserService userService;
    private final AppConfigService appConfigService;
    private final AliYunDriveService aliYunDriveService;
    private final AliYunOpenService aliYunOpenService;
    private final StorageService storageService;
    private final AListClient aListClient;
    private final HubProperties hubProperties;
    private final AListService aListService;
    @GetMapping("/security")
    public List<SecurityDTO> getSecurity() {
        List<SecurityDTO> securityDTOList = new ArrayList<>();
        Optional<User> optional = userService.findByUsername("guest");
        Optional<Storage> ali = storageService.findById(Constants.MY_ALI_ID);
        if (optional.isPresent()) {
            User user = optional.get();
            boolean value = user.getDisabled() != null && user.getDisabled() != 0;
            securityDTOList.add(new SecurityDTO(value, "guest"));
        } else {
            securityDTOList.add(new SecurityDTO(false, "guest"));
        }
        securityDTOList.add(new SecurityDTO(ali.map(Storage::isDisabled).orElse(false), "ali"));
        securityDTOList.add(new SecurityDTO(appConfigService.findById(Constants.TV_BOX_TOKEN).isPresent(), "tvbox"));
        return securityDTOList;
    }

    @PutMapping("/security")
    public void updateSecurity(@RequestBody @Valid SecurityDTO securityDTO) {
        switch (securityDTO.getLabel()) {
            case "ali":
                if (securityDTO.getValue()) {
                    aListClient.enable(Constants.MY_ALI_ID);
                } else {
                    aListClient.disable(Constants.MY_ALI_ID);
                }
                break;
            case "tvbox":
                AppConfig appConfig = new AppConfig();
                appConfig.setId(Constants.TV_BOX_TOKEN);
                appConfig.setGroup(0);
                if (securityDTO.getValue()) {
                    appConfig.setValue(RandomUtils.generateRandomString(64));
                    appConfigService.save(appConfig);
                } else {
                    appConfigService.deleteById(appConfig.getId());
                }
                break;
            default:
                throw new ServiceException("Unknown label: " + securityDTO.getLabel());
        }
    }

    @PutMapping("/ali")
    public void updateAli(@RequestBody @Valid AccountDTO account) {
        switch (account.getType()) {
            case "drive":
                String status = aliYunDriveService.authorize(account.getParams());
                if (!"CONFIRMED".equals(status)) {
                    throw new ServiceException("校验失败");
                }
//                updateStorage("AliyundriveShare2Open");
                break;
            case "openapi":
                if (account.getParams().containsKey("url")) {
                    aliYunOpenService.authorize(account.getParams().get("url").toString());
                } else {
                    throw new IllegalArgumentException("参数url未填写");
                }
                storageService.findById(Constants.MY_ALI_ID).ifPresent(storageService::flush);
                updateStorage("AliyundriveShare2Open");
                break;
            default:
                throw new ServiceException("未知类型");
        }
    }

    private void updateStorage(String driver) {
        new Thread(() -> {
            List<Storage> storages = storageService.findAllByDriver(driver);
            storages.forEach(storageService::flush);
        }).start();
    }

    @GetMapping("/ali")
    public AccountVO getAli() {
        Optional<AliYunDriveBO> al = appConfigService.get(new AliYunDriveBO(), AliYunDriveBO.class);
        AccountVO accountVO = new AccountVO();
        accountVO.setUsername(al.map(AliYunDriveBO::getUserName).orElse(null));
        return accountVO;
    }

    @GetMapping("/config")
    @SneakyThrows
    public ConfigVO getConfig() {
        String config = Files.readString(Path.of(hubProperties.getPath() + "/config.json"));
        JsonNode jsonNode = JsonUtils.readTree(config);
        ConfigVO configVO = new ConfigVO();
        configVO.setOpenTokenUrl(jsonNode.findPath("opentoken_auth_url").asText());
        configVO.setSiteUrl(jsonNode.findPath("site_url").asText());
        return configVO;
    }

    @PutMapping("/config")
    @SneakyThrows
    public void updateConfig(@RequestBody ConfigVO configVO) {
        String config = Files.readString(Path.of(hubProperties.getPath() + "/config.json"));
        JsonNode jsonNode = JsonUtils.readTree(config);
        ObjectNode objectNode = (ObjectNode) jsonNode;
        if (StringUtils.hasText(configVO.getOpenTokenUrl())) {
            objectNode.put("opentoken_auth_url", configVO.getOpenTokenUrl());
        }
        if (StringUtils.hasText(configVO.getSiteUrl())) {
            objectNode.put("site_url", configVO.getSiteUrl());
            objectNode.put("token_expires_in", 4800);
        }
        Files.writeString(Path.of(hubProperties.getPath() + "/config.json"), JsonUtils.toJson(objectNode));
        aListService.updateTvBox(configVO.getSiteUrl());
    }

    @GetMapping("/aria2")
    @SneakyThrows
    public Aria2BO getAria2() {
        return appConfigService.get(new Aria2BO(), Aria2BO.class).orElse(null);
    }

    @PutMapping("/aria2")
    @SneakyThrows
    public void updateAria2(@RequestBody Aria2BO aria2BO) {
        appConfigService.saveOrUpdate(aria2BO);
    }

    @GetMapping("/drive/{id}")
    @SneakyThrows
    public Map<String, Object> getDrive(@PathVariable("id") String id) {
        return appConfigService.get(id);
    }

    @PutMapping("/drive/{id}")
    @SneakyThrows
    public void updateDrive(@PathVariable("id") String id, @RequestBody Map<String, Object> params) {
        Optional<AppConfig> appConfig = appConfigService.findById(id);
        AppConfig c;
        if (appConfig.isPresent()) {
            c = appConfig.get();
            c.setValue(JsonUtils.toJson(params));
        } else {
            c = new AppConfig();
            c.setValue(JsonUtils.toJson(params));
            c.setId(id);
            c.setGroup(Constants.ALIST_GROUP);
        }
        appConfigService.save(c);
        updateStorage(id);
    }

}

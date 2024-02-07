package org.alist.hub.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.alist.hub.bean.Constants;
import org.alist.hub.bo.AliYunDriveBO;
import org.alist.hub.bo.NoticeBO;
import org.alist.hub.bo.PikPakBo;
import org.alist.hub.configure.HubProperties;
import org.alist.hub.dto.AccountDTO;
import org.alist.hub.dto.SecurityDTO;
import org.alist.hub.exception.ServiceException;
import org.alist.hub.external.AListClient;
import org.alist.hub.model.AppConfig;
import org.alist.hub.model.Storage;
import org.alist.hub.model.User;
import org.alist.hub.repository.AppConfigRepository;
import org.alist.hub.repository.StorageRepository;
import org.alist.hub.repository.UserRepository;
import org.alist.hub.service.AliYunDriveService;
import org.alist.hub.service.AliYunOpenService;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.service.StorageService;
import org.alist.hub.util.JsonUtils;
import org.alist.hub.util.RandomUtils;
import org.alist.hub.util.StringUtils;
import org.alist.hub.vo.AccountVO;
import org.alist.hub.vo.ConfigVO;
import org.springframework.web.bind.annotation.GetMapping;
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
public class SettingController {
    private final UserRepository userRepository;
    private final AppConfigService appConfigService;
    private final AppConfigRepository appConfigRepository;
    private final AliYunDriveService aliYunDriveService;
    private final AliYunOpenService aliYunOpenService;
    private final StorageRepository storageRepository;
    private final StorageService storageService;
    private final AListClient aListClient;
    private final HubProperties hubProperties;

    @GetMapping("/security")
    public List<SecurityDTO> getSecurity() {
        List<SecurityDTO> securityDTOList = new ArrayList<>();
        Optional<User> optional = userRepository.findByUsername("guest");
        Optional<Storage> ali = storageRepository.findById(Constants.MY_ALI_ID);
        if (optional.isPresent()) {
            User user = optional.get();
            boolean value = user.getDisabled() != null && user.getDisabled() != 0;
            securityDTOList.add(new SecurityDTO(value, "guest"));
        } else {
            securityDTOList.add(new SecurityDTO(false, "guest"));
        }
        securityDTOList.add(new SecurityDTO(ali.map(Storage::isDisabled).orElse(false), "ali"));
        securityDTOList.add(new SecurityDTO(appConfigRepository.findById(Constants.TV_BOX_TOKEN).isPresent(), "tvbox"));
        return securityDTOList;
    }

    @PutMapping("/security")
    public void updateSecurity(@RequestBody @Valid SecurityDTO securityDTO) {
        switch (securityDTO.getLabel()) {
            case "ali":
                if (securityDTO.getValue()) {
                    aListClient.disable(Constants.MY_ALI_ID);
                } else {
                    aListClient.enable(Constants.MY_ALI_ID);
                }
                break;
            case "tvbox":
                AppConfig appConfig = new AppConfig();
                appConfig.setId(Constants.TV_BOX_TOKEN);
                appConfig.setGroup(0);
                if (securityDTO.getValue()) {
                    appConfig.setValue(RandomUtils.generateRandomString(64));
                    appConfigRepository.save(appConfig);
                } else {
                    appConfigRepository.deleteById(appConfig.getId());
                }
                break;
            default:
                throw new ServiceException("Unknown label: " + securityDTO.getLabel());
        }
    }

    @PutMapping("/account")
    public void updateAccount(@RequestBody @Valid AccountDTO account) {
        List<Storage> storages;
        switch (account.getType()) {
            case "drive":
                String status = aliYunDriveService.authorize(account.getParams());
                if (!"CONFIRMED".equals(status)) {
                    throw new ServiceException("校验失败");
                }
                storages = storageRepository.findAllByDriver("AliyundriveShare2Open");
                storages.forEach(storageService::flush);
                break;
            case "openapi":
                if (account.getParams().containsKey("url")) {
                    aliYunOpenService.authorize(account.getParams().get("url").toString());
                } else {
                    throw new IllegalArgumentException("参数url未填写");
                }
                storages = storageRepository.findAllByDriver("AliyundriveShare2Open");
                storageRepository.findById(Constants.MY_ALI_ID).ifPresent(storageService::flush);
                storages.forEach(storageService::flush);
                break;
            case "pikpak":
                Map<String, Object> params = account.getParams();
                if (params.containsKey("username") && params.containsKey("password")) {
                    PikPakBo pikPakBo = new PikPakBo();
                    pikPakBo.setPassword(params.get("password").toString());
                    pikPakBo.setUsername(params.get("username").toString());
                    appConfigService.saveOrUpdate(pikPakBo);
                }
                storages = storageRepository.findAllByDriver("PikPakShare");
                storages.forEach(storageService::flush);
                break;
            default:
                throw new ServiceException("未知类型");
        }
    }

    @GetMapping("/account")
    public AccountVO getAccount() {
        Optional<AliYunDriveBO> al = appConfigService.get(new AliYunDriveBO(), AliYunDriveBO.class);
        Optional<PikPakBo> pk = appConfigService.get(new PikPakBo(), PikPakBo.class);
        Optional<NoticeBO> notice = appConfigService.get(new NoticeBO(), NoticeBO.class);
        AccountVO accountVO = new AccountVO();
        accountVO.setPikpak(pk.map(PikPakBo::getUsername).orElse(null));
        accountVO.setUsername(al.map(AliYunDriveBO::getUserName).orElse(null));
        accountVO.setPushKey(notice.map(NoticeBO::getPushKey).orElse(null));
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
        }
        Files.writeString(Path.of(hubProperties.getPath() + "/config.json"), JsonUtils.toJson(objectNode));
    }
}

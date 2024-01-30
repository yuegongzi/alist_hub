package org.alist.hub.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.alist.hub.api.AListClient;
import org.alist.hub.bean.Constants;
import org.alist.hub.bo.AliYunDriveBO;
import org.alist.hub.bo.PikPakBo;
import org.alist.hub.configure.HubProperties;
import org.alist.hub.dto.AccountDTO;
import org.alist.hub.dto.SecurityDTO;
import org.alist.hub.exception.ServiceException;
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
import org.alist.hub.utils.JsonUtil;
import org.alist.hub.utils.RandomUtil;
import org.alist.hub.utils.StringUtils;
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
    public List<SecurityDTO> get() {
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
    public void security(@RequestBody @Valid SecurityDTO securityDTO) {
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
                    appConfig.setValue(RandomUtil.generateRandomString(64));
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
    public void account(@RequestBody @Valid AccountDTO accountDTO) {
        List<Storage> storages;
        switch (accountDTO.getType()) {
            case "drive":
                String status = aliYunDriveService.authorize(accountDTO.getParams());
                if (!"CONFIRMED".equals(status)) {
                    throw new ServiceException("校验失败");
                }
                storages = storageRepository.findAllByDriver("AliyundriveShare2Open");
                storages.forEach(storageService::flush);
                break;
            case "openapi":
                if (accountDTO.getParams().containsKey("url")) {
                    aliYunOpenService.authorize(accountDTO.getParams().get("url").toString());
                } else {
                    throw new IllegalArgumentException("参数url未填写");
                }
                storages = storageRepository.findAllByDriver("AliyundriveShare2Open");
                storageRepository.findById(Constants.MY_ALI_ID).ifPresent(storageService::flush);
                storages.forEach(storageService::flush);
                break;
            case "pikpak":
                Map<String, Object> params = accountDTO.getParams();
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
        AliYunDriveBO aliYunDriveBO = new AliYunDriveBO();
        PikPakBo pikPakBo = new PikPakBo();
        Optional<AliYunDriveBO> al = appConfigService.get(aliYunDriveBO, AliYunDriveBO.class);
        Optional<PikPakBo> pk = appConfigService.get(pikPakBo, PikPakBo.class);
        AccountVO accountVO = new AccountVO();
        accountVO.setPikpak(pk.map(PikPakBo::getUsername).orElse(null));
        accountVO.setUsername(al.map(AliYunDriveBO::getUserName).orElse(null));
        return accountVO;
    }

    @GetMapping("/config")
    @SneakyThrows
    public ConfigVO getConfig() {
        String config = Files.readString(Path.of(hubProperties.getPath() + "/config.json"));
        JsonNode jsonNode = JsonUtil.readTree(config);
        ConfigVO configVO = new ConfigVO();
        configVO.setOpenTokenUrl(jsonNode.findPath("opentoken_auth_url").asText());
        configVO.setSiteUrl(jsonNode.findPath("site_url").asText());
        return configVO;
    }

    @PutMapping("/config")
    @SneakyThrows
    public void updateConfig(@RequestBody ConfigVO configVO) {
        String config = Files.readString(Path.of(hubProperties.getPath() + "/config.json"));
        JsonNode jsonNode = JsonUtil.readTree(config);
        ObjectNode objectNode = (ObjectNode) jsonNode;
        if (StringUtils.hasText(configVO.getOpenTokenUrl())) {
            objectNode.put("opentoken_auth_url", configVO.getOpenTokenUrl());
        }
        if (StringUtils.hasText(configVO.getSiteUrl())) {
            objectNode.put("site_url", configVO.getSiteUrl());
        }
        Files.writeString(Path.of(hubProperties.getPath() + "/config.json"), JsonUtil.toJson(objectNode));
    }
}

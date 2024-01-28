package org.alist.hub.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.alist.hub.bean.Constants;
import org.alist.hub.bo.AliYunDriveBO;
import org.alist.hub.bo.PikPakBo;
import org.alist.hub.dto.AccountDTO;
import org.alist.hub.dto.SecurityDTO;
import org.alist.hub.exception.ServiceException;
import org.alist.hub.model.AppConfig;
import org.alist.hub.model.User;
import org.alist.hub.repository.AppConfigRepository;
import org.alist.hub.repository.UserRepository;
import org.alist.hub.service.AliYunDriveService;
import org.alist.hub.service.AliYunOpenService;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.service.StorageService;
import org.alist.hub.utils.RandomUtil;
import org.alist.hub.vo.AccountVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private final StorageService storageService;
    private final AppConfigRepository appConfigRepository;
    private final AliYunDriveService aliYunDriveService;
    private final AliYunOpenService aliYunOpenService;

    @GetMapping("/security")
    public List<SecurityDTO> get() {
        List<SecurityDTO> securityDTOList = new ArrayList<>();
        Optional<User> optional = userRepository.findByUsername("guest");
        if (optional.isPresent()) {
            User user = optional.get();
            boolean value = user.getDisabled() == null || user.getDisabled() == 0;
            securityDTOList.add(new SecurityDTO(value, "guest"));
        } else {
            securityDTOList.add(new SecurityDTO(false, "guest"));
        }
        securityDTOList.add(new SecurityDTO(!storageService.getMyAli(), "ali"));
        securityDTOList.add(new SecurityDTO(appConfigRepository.findById(Constants.TV_BOX_TOKEN).isPresent(), "tvbox"));
        return securityDTOList;
    }

    @PutMapping("/security")
    public void security(@RequestBody @Valid SecurityDTO securityDTO) {
        switch (securityDTO.getLabel()) {
            case "guest":
                Optional<User> optional = userRepository.findByUsername("guest");
                optional.map(user -> {
                    user.setDisabled(securityDTO.getValue() ? 1 : 0);
                    return userRepository.save(user);
                });
                break;
            case "ali":
                storageService.updateMyAli(!securityDTO.getValue());
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
        switch (accountDTO.getType()) {
            case "drive":
                String status = aliYunDriveService.authorize(accountDTO.getParams());
                if (!"CONFIRMED".equals(status)) {
                    throw new ServiceException("校验失败");
                }
                storageService.updateAliYunDrive();
                break;
            case "openapi":
                if (accountDTO.getParams().containsKey("url")) {
                    aliYunOpenService.authorize(accountDTO.getParams().get("url").toString());
                    storageService.updateAliYunDrive();
                } else {
                    throw new IllegalArgumentException("参数url未填写");
                }
                break;
            case "pikpak":
                Map<String, Object> params = accountDTO.getParams();
                if (params.containsKey("username") && params.containsKey("password")) {
                    PikPakBo pikPakBo = new PikPakBo();
                    pikPakBo.setPassword(params.get("password").toString());
                    pikPakBo.setUsername(params.get("username").toString());
                    appConfigService.saveOrUpdate(pikPakBo);
                    storageService.updatePikPak();
                }
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
}

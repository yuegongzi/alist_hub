package org.alist.hub.controller;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.alist.hub.bean.DriveInfo;
import org.alist.hub.bean.SpaceInfo;
import org.alist.hub.bean.UserClaims;
import org.alist.hub.bo.AliYunSignBO;
import org.alist.hub.dto.PasswordDTO;
import org.alist.hub.external.AliYunDriveClient;
import org.alist.hub.external.AliYunOpenClient;
import org.alist.hub.model.User;
import org.alist.hub.repository.UserRepository;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.util.RequestContextUtil;
import org.alist.hub.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final AliYunDriveClient aliYunDriveClient;
    private final AliYunOpenClient aliYunOpenClient;
    private final AppConfigService appConfigService;

    @PutMapping("/password")
    public void update(@RequestBody @Valid PasswordDTO passwordDTO) {
        if (StringUtils.equal(passwordDTO.getNewPassword(), passwordDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("两次输入密码不一致");
        }
        UserClaims userClaims = RequestContextUtil.getUserClaims();
        Optional<User> user = userRepository.findByUsername(userClaims.getUsername());
        if (user.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (StringUtils.equal(user.get().getPassword(), passwordDTO.getPassword())) {
            throw new IllegalArgumentException("原始密码输入错误");
        }
        User u = user.get();
        u.setPassword(passwordDTO.getNewPassword());
        userRepository.save(u);
    }

    /**
     * 获取用户签到列表
     */
    @GetMapping("/sign")
    public JsonNode sign() {
        AliYunSignBO aliYunSignBO = new AliYunSignBO();
        Optional<AliYunSignBO> optional = appConfigService.get(aliYunSignBO, AliYunSignBO.class);
        if (optional.isEmpty()) {
            JsonNode jsonNode = aliYunDriveClient.sign();
            aliYunSignBO.setResult(jsonNode);
            appConfigService.saveOrUpdate(aliYunSignBO);
            return jsonNode;
        }
        return optional.get().getResult();
    }

    @GetMapping()
    public Map<String, Object> aliyunDrive() {
        Map<String, Object> map = new HashMap<>();
        Optional<DriveInfo> driveInfo = aliYunOpenClient.getDriveInfo();
        Optional<SpaceInfo> spaceInfo = aliYunOpenClient.getSpaceInfo();
        driveInfo.ifPresent(d -> {
            map.put("driveInfo", driveInfo.get());
        });
        spaceInfo.ifPresent(d -> {
            map.put("spaceInfo", spaceInfo.get());
        });
        return map;
    }

    /**
     * 用户授权检查
     */
    @GetMapping("/authorized")
    public void authorize() {

    }
}

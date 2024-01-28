package org.alist.hub.api;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.Response;
import org.alist.hub.bo.AliYunDriveBO;
import org.alist.hub.bo.Persistent;
import org.alist.hub.exception.ServiceException;
import org.alist.hub.model.AppConfig;
import org.alist.hub.repository.AppConfigRepository;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.utils.JsonUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
public class AliYunDriveClient {
    private final Http http;
    private final AppConfigRepository appConfigRepository;
    private final AppConfigService appConfigService;

    private String refreshToken(AliYunDriveBO aliYunDriveBO) {
        // 发送HTTP请求获取新的token
        Response response = http.post(Payload.create("https://auth.aliyundrive.com/v2/account/token")
                .addBody("grant_type", "refresh_token")
                .addBody("refresh_token", aliYunDriveBO.getRefreshToken())
        );
        JsonNode jsonNode = response.asJsonNode();
        if (!jsonNode.has("access_token")) {
            throw new ServiceException("获取token失败");
        }
        aliYunDriveBO.setExpiresIn(jsonNode.findValue("expires_in").asLong() * 900 + System.currentTimeMillis());//少存一点时间
        aliYunDriveBO.setRefreshToken(jsonNode.findValue("refresh_token").asText());
        aliYunDriveBO.setAccessToken(jsonNode.findValue("access_token").asText());
        appConfigService.saveOrUpdate(aliYunDriveBO);
        return aliYunDriveBO.getAccessToken();
    }

    private String getAccessToken() {
        Persistent persistent = new AliYunDriveBO();
        Optional<AppConfig> appConfig = appConfigRepository.findById(persistent.getId());  // 根据persistent的id获取App配置
        if (appConfig.isEmpty()) {  // 如果App配置为空
            throw new ServiceException("获取token失败");
        }
        Optional<AliYunDriveBO> aliYunDriveBO = JsonUtil.readValue(appConfig.get().getValue(), AliYunDriveBO.class);
        if (aliYunDriveBO.isEmpty()) {  // 如果AliyunOpenBO对象为空
            throw new ServiceException("获取token失败");
        }
        Long expire = aliYunDriveBO.get().getExpiresIn();
        if (!StringUtils.hasText(aliYunDriveBO.get().getAccessToken()) || expire == null  // 如果AliyunOpenBO对象的访问令牌为空或者过期时间为空
                || expire <= System.currentTimeMillis()) {  // 或者过期时间大于当前时间
            return refreshToken(aliYunDriveBO.get());  // 通过刷新令牌获取新的访问令牌
        }
        return aliYunDriveBO.get().getAccessToken();
    }

    public JsonNode sign() {
        Payload payload = Payload.create("https://member.aliyundrive.com/v1/activity/sign_in_list")
                .addHeader("Authorization", "Bearer " + getAccessToken());
        return http.post(payload).asJsonNode().findValue("result");
    }
}

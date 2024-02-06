package org.alist.hub.api;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.Constants;
import org.alist.hub.bean.ExpiringMap;
import org.alist.hub.bean.FileWatcher;
import org.alist.hub.bean.Response;
import org.alist.hub.bean.ShareFile;
import org.alist.hub.bo.AliYunDriveBO;
import org.alist.hub.bo.Persistent;
import org.alist.hub.exception.ServiceException;
import org.alist.hub.model.AppConfig;
import org.alist.hub.repository.AppConfigRepository;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.utils.JsonUtil;
import org.alist.hub.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
public class AliYunDriveClient {
    private final Http http;
    private final AppConfigRepository appConfigRepository;
    private final AppConfigService appConfigService;
    private final static ExpiringMap<String, String> expiringMap = new ExpiringMap<>();

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
                .addHeader("Authorization", "Bearer " + getAccessToken())
                .addHeader("user-agent", Constants.USER_AGENT);
        return http.post(payload).asJsonNode().findValue("result");
    }

    /**
     * 获取分享链接的token
     *
     * @param shareId  分享ID
     * @param sharePwd 分享密码
     * @return 可选的分享链接token
     */
    public Optional<String> getShareToken(String shareId, String sharePwd) {
        String shareToken = expiringMap.get(shareId);
        if (StringUtils.hasText(shareToken)) {
            return Optional.of(shareToken);
        }
        Payload payload = Payload.create("https://api.aliyundrive.com/v2/share_link/get_share_token");
        payload.addHeader("user-agent", Constants.USER_AGENT);
        payload.addBody("share_id", shareId);
        payload.addBody("share_pwd", sharePwd);
        Response response = http.post(payload);
        if (response.getStatusCode().is2xxSuccessful()) {
            shareToken = response.asJsonNode().findValue("share_token").asText();
            expiringMap.put(shareId, shareToken, 7000 * 1000);
            return Optional.of(shareToken);
        }
        return Optional.empty();
    }

    public List<ShareFile> getShareList(String shareId, String sharePwd, String parentFileId) {
        Optional<String> shareToken = getShareToken(shareId, sharePwd);
        if (shareToken.isEmpty()) {
            throw new ServiceException("获取分享链接失败");
        }
        Payload payload = Payload.create("https://api.aliyundrive.com/adrive/v2/file/list_by_share");
        payload.addHeader("user-agent", Constants.USER_AGENT)
                .addHeader("x-share-token", shareToken.get())
                .addBody("parent_file_id", parentFileId)
                .addBody("share_id", shareId)
                .addBody("limit", 100);
        Response response = http.post(payload);
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.asList(ShareFile.class, "items");
        }
        return new ArrayList<>();
    }

    public boolean copy(FileWatcher fileWatcher, String shareId, String sharePwd, List<String> fileIds) {
        List<Map<String, Object>> requests = new ArrayList<>();
        for (int i = 0; i < fileIds.size(); i++) {
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> body = new HashMap<>();
            Map<String, Object> headers = new HashMap<>();
            body.put("file_id", fileIds.get(i));
            body.put("share_id", shareId);
            body.put("auto_rename", true);
            body.put("to_parent_file_id", fileWatcher.getToFileId());
            body.put("to_drive_id", fileWatcher.getToDriveId());
            headers.put("Content-Type", "application/json");
            content.put("id", String.format("%s", i++));
            content.put("method", "POST");
            content.put("url", "/file/copy");
            content.put("body", body);
            content.put("headers", headers);
            requests.add(content);
        }
        Payload payload = Payload.create("https://api.aliyundrive.com/adrive/v4/batch")
                .addHeader("user-agent", Constants.USER_AGENT)
                .addHeader("x-share-token", getShareToken(shareId, sharePwd).get())
                .addHeader("Authorization", "Bearer " + getAccessToken())
                .addBody("requests", requests)
                .addBody("resource", "file");
        return http.post(payload).getStatusCode().is2xxSuccessful();
    }
}

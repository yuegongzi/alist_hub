package org.alist.hub.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.alist.hub.bean.Constants;
import org.alist.hub.bean.FileInfo;
import org.alist.hub.bo.AliYunOpenBO;
import org.alist.hub.client.Http;
import org.alist.hub.client.Payload;
import org.alist.hub.client.Response;
import org.alist.hub.exception.ServiceException;
import org.alist.hub.external.AliYunOpenClient;
import org.alist.hub.service.AliYunOpenService;
import org.alist.hub.service.AppConfigService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@AllArgsConstructor
@Service
public class AliYunOpenServiceImpl implements AliYunOpenService {
    private final AppConfigService appConfigService;
    private final AliYunOpenClient aliYunOpenClient;

    @Override
    public void authorize(String url) {
        Response response = Http.get(Payload.create(Constants.API_DOMAIN + "/proxy/" + url));
        JsonNode jsonNode = response.asJsonNode();
        // 获取authCode字段的值，若不存在则为空字符串
        String authCode = jsonNode.findValue("authCode") != null ? jsonNode.findValue("authCode").asText() : "";
        // 如果authCode非空，则进行进一步处理
        if (StringUtils.hasText(authCode)) {
            // 创建请求Payload，并添加授权代码和其他参数
            Payload payload = Payload.create(Constants.API_DOMAIN + "/alist/ali_open/code");
            payload.addBody("code", authCode);
            payload.addBody("grant_type", "authorization_code");
            // 发送POST请求获取新的refresh_token
            Response res = Http.post(payload);
            Optional<AliYunOpenBO> optional = res.asValue(AliYunOpenBO.class);
            if (optional.isEmpty()) {
                throw new ServiceException("授权回调失败");
            }
            AliYunOpenBO aliYunOpenBO = optional.get();
            if (!StringUtils.hasText(aliYunOpenBO.getRefreshToken())) {
                throw new ServiceException("授权回调失败");
            }
            appConfigService.saveOrUpdate(aliYunOpenBO);
            // 更新ExpiresIn字段的值
            aliYunOpenBO.setExpiresIn(aliYunOpenBO.getExpiresIn() * 900 + System.currentTimeMillis());//少存一点时间
            // 在根目录创建文件夹
            Optional<FileInfo> fileInfo = aliYunOpenClient.createFolder(Constants.FILE_NAME, "resource", "root");
            // 检查文件夹创建是否成功
            if (fileInfo.isEmpty()) {
                throw new ServiceException("创建文件夹失败");
            }
            aliYunOpenBO.setName(fileInfo.get().getFileName());
            aliYunOpenBO.setName(fileInfo.get().getFileName());
            aliYunOpenBO.setFolderId(fileInfo.get().getFileId());
            aliYunOpenBO.setDriveId(fileInfo.get().getDriveId());
            // 保存或更新配置信息
            appConfigService.saveOrUpdate(aliYunOpenBO);

        }
    }

}

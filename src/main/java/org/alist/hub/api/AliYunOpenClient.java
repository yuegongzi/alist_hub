package org.alist.hub.api;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.DriveInfo;
import org.alist.hub.bean.FileInfo;
import org.alist.hub.bean.Response;
import org.alist.hub.bo.AliYunOpenBO;
import org.alist.hub.bo.Persistent;
import org.alist.hub.exception.ServiceException;
import org.alist.hub.model.AppConfig;
import org.alist.hub.repository.AppConfigRepository;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.utils.JsonUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Optional;

/**
 * 阿里云盘开放平台相关接口
 */
@Component
@AllArgsConstructor
@Slf4j
public class AliYunOpenClient {
    private final AppConfigRepository appConfigRepository;
    private final AppConfigService appConfigService;
    private final Http http;


    // 刷新token
    private String refreshToken(String refreshToken) throws IOException {
        // 发送HTTP请求获取新的token
        Response response = http.post(Payload.create("https://api.nn.ci/alist/ali_open/code")
                .addBody("grant_type", "refresh_token")
                .addBody("refresh_token", refreshToken)
        );
        Optional<AliYunOpenBO> optional = response.asValue(AliYunOpenBO.class);
        if (optional.isEmpty()) {
            throw new ServiceException("获取token失败");
        }
        AliYunOpenBO aliyunOpenBO = optional.get();
        aliyunOpenBO.setExpiresIn(aliyunOpenBO.getExpiresIn() * 900 + System.currentTimeMillis());//少存一点时间
        // 保存或更新AliyunOpenBO对象到数据库
        appConfigService.saveOrUpdate(aliyunOpenBO);
        return aliyunOpenBO.getAccessToken();
    }


    /**
     * 获取访问令牌
     *
     * @return 访问令牌字符串
     */
    private String getAccessToken() {
        Persistent persistent = new AliYunOpenBO();
        Optional<AppConfig> appConfig = appConfigRepository.findById(persistent.getId());  // 根据persistent的id获取App配置
        if (appConfig.isEmpty()) {  // 如果App配置为空
            throw new ServiceException("获取token失败");
        }
        Optional<AliYunOpenBO> aliyunOpenBO = JsonUtil.readValue(appConfig.get().getValue(), AliYunOpenBO.class);
        if (aliyunOpenBO.isEmpty()) {  // 如果AliyunOpenBO对象为空
            throw new ServiceException("获取token失败");
        }
        Long expire = aliyunOpenBO.get().getExpiresIn();
        if (!StringUtils.hasText(aliyunOpenBO.get().getAccessToken()) || expire == null  // 如果AliyunOpenBO对象的访问令牌为空或者过期时间为空
                || expire >= System.currentTimeMillis()) {  // 或者过期时间大于当前时间
            try {
                return refreshToken(aliyunOpenBO.get().getRefreshToken());  // 通过刷新令牌获取新的访问令牌
            } catch (IOException e) {
                throw new ServiceException("获取token失败");
            }
        }
        return aliyunOpenBO.get().getAccessToken();
    }


    private Payload createPayload(String path) {
        return Payload.create("https://openapi.alipan.com" + path)
                .addHeader("Authorization", "Bearer " + getAccessToken());
    }


    public Optional<DriveInfo> getDriveInfo() {
        return http.post(createPayload("/adrive/v1.0/user/getDriveInfo")).asValue(DriveInfo.class);

    }

    /**
     * 创建文件
     *
     * @param name           文件名
     * @param type           文件类型
     * @param parent_file_id 父文件ID
     * @return 返回一个Mono类型的文件信息
     */
    public Optional<FileInfo> createFile(String name, String type, String parent_file_id) {
        Optional<DriveInfo> driveInfo = getDriveInfo();
        if (driveInfo.isPresent()) {
            Response response = http.post(createPayload("/adrive/v1.0/openFile/create")
                    .addBody("check_name_mode", "refuse")
                    .addBody("type", type)
                    .addBody("parent_file_id",
                            StringUtils.hasText(parent_file_id) ? parent_file_id : "root")
                    .addBody("drive_id", driveInfo.get().getResource_drive_id())
                    .addBody("name", name));
            return response.asValue(FileInfo.class);
        }
        return Optional.empty();
    }

}

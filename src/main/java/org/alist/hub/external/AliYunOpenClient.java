package org.alist.hub.external;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.Constants;
import org.alist.hub.bean.DriveInfo;
import org.alist.hub.bean.FileInfo;
import org.alist.hub.bean.FileItem;
import org.alist.hub.bean.SpaceInfo;
import org.alist.hub.bo.AliYunOpenBO;
import org.alist.hub.bo.Persistent;
import org.alist.hub.client.Http;
import org.alist.hub.client.Payload;
import org.alist.hub.client.Response;
import org.alist.hub.exception.ServiceException;
import org.alist.hub.model.AppConfig;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.util.JsonUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 阿里云盘开放平台相关接口
 */
@Component
@AllArgsConstructor
@Slf4j
public class AliYunOpenClient {
    private final AppConfigService appConfigService;


    // 刷新token
    private String refreshToken(String refreshToken) {
        // 发送HTTP请求获取新的token
        Response response = Http.post(Payload.create(Constants.API_DOMAIN + "/alist/ali_open/code")
                .addBody("grant_type", "refresh_token")
                .addBody("refresh_token", refreshToken)
        );
        Optional<AliYunOpenBO> optional = response.asValue(AliYunOpenBO.class);
        if (optional.isEmpty()) {
            throw new ServiceException("获取token失败");
        }
        AliYunOpenBO aliyunOpenBO = optional.get();
        if (!StringUtils.hasText(aliyunOpenBO.getAccessToken())) {
            throw new ServiceException("获取token失败");
        }
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
        Optional<AppConfig> appConfig = appConfigService.findById(persistent.getId());  // 根据persistent的id获取App配置
        if (appConfig.isEmpty()) {  // 如果App配置为空
            throw new ServiceException("获取token失败");
        }
        Optional<AliYunOpenBO> aliyunOpenBO = JsonUtils.readValue(appConfig.get().getValue(), AliYunOpenBO.class);
        if (aliyunOpenBO.isEmpty()) {  // 如果AliyunOpenBO对象为空
            throw new ServiceException("获取token失败");
        }
        Long expire = aliyunOpenBO.get().getExpiresIn();
        if (!StringUtils.hasText(aliyunOpenBO.get().getAccessToken()) || expire == null  // 如果AliyunOpenBO对象的访问令牌为空或者过期时间为空
                || expire <= System.currentTimeMillis()) {  // 或者过期时间大于当前时间
            return refreshToken(aliyunOpenBO.get().getRefreshToken());  // 通过刷新令牌获取新的访问令牌
        }
        return aliyunOpenBO.get().getAccessToken();
    }


    private Payload createPayload(String path) {
        return Payload.create("https://openapi.alipan.com" + path)
                .addHeader("Authorization", "Bearer " + getAccessToken());
    }


    /**
     * 获取用户驱动信息
     *
     * @return 用户驱动信息
     */
    public Optional<DriveInfo> getDriveInfo() {
        return Http.post(createPayload("/adrive/v1.0/user/getDriveInfo")).asValue(DriveInfo.class);
    }


    /**
     * 获取空间信息
     *
     * @return 空间信息
     */
    public Optional<SpaceInfo> getSpaceInfo() {
        return Http.post(createPayload("/adrive/v1.0/user/getSpaceInfo")).asValue(SpaceInfo.class, "personal_space_info");
    }


    /**
     * 创建文件
     *
     * @param name           文件名
     * @param type           类型 backup | resource
     * @param parent_file_id 父文件ID
     * @return 返回一个Mono类型的文件信息
     */
    public Optional<FileInfo> createFolder(String name, String type, String parent_file_id) {
        Optional<DriveInfo> driveInfo = getDriveInfo();
        if (driveInfo.isPresent()) {
            String driverId = Objects.equals(type, "backup") ? driveInfo.get().getBackupDriveId() : driveInfo.get().getResourceDriveId();
            Response response = Http.post(createPayload("/adrive/v1.0/openFile/create")
                    .addBody("check_name_mode", "refuse")
                    .addBody("type", "folder")
                    .addBody("parent_file_id",
                            StringUtils.hasText(parent_file_id) ? parent_file_id : "root")
                    .addBody("drive_id", driverId)
                    .addBody("name", name));
            return response.asValue(FileInfo.class);
        }
        return Optional.empty();
    }

    /**
     * 获取文件列表
     *
     * @param driveId      驱动ID
     * @param parentFileId 父文件ID
     * @return 文件列表
     */
    public List<FileItem> getFileList(String driveId, String parentFileId) {
        return Http.post(createPayload("/adrive/v1.0/openFile/list")
                .addBody("drive_id", driveId)
                .addBody("parent_file_id", parentFileId)).asList(FileItem.class, "items");
    }


    /**
     * 删除文件
     *
     * @param driveId 驱动ID
     * @param fileId  文件ID
     * @return 删除是否成功
     */
    public boolean deleteFile(String driveId, String fileId) {
        return Http.post(createPayload("/adrive/v1.0/openFile/delete")
                .addBody("drive_id", driveId)
                .addBody("file_id", fileId)).getStatusCode().is2xxSuccessful();
    }

}

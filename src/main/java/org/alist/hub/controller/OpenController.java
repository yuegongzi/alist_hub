package org.alist.hub.controller;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.api.AliyunOpen;
import org.alist.hub.api.Http;
import org.alist.hub.api.Payload;
import org.alist.hub.bean.Constants;
import org.alist.hub.bean.FileInfo;
import org.alist.hub.bean.Response;
import org.alist.hub.bo.AliyunDriveBO;
import org.alist.hub.bo.AliyunFolderBo;
import org.alist.hub.bo.AliyunOpenBO;
import org.alist.hub.dto.InitializeDTO;
import org.alist.hub.exception.ServiceException;
import org.alist.hub.model.User;
import org.alist.hub.repository.UserRepository;
import org.alist.hub.service.AListService;
import org.alist.hub.service.AppConfigService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

/**
 * 定义所有的开放接口 允许未登入访问
 */
@RequestMapping("/open")
@RestController
@AllArgsConstructor
@Slf4j
public class OpenController {
    private final Http http;
    private final AppConfigService appConfigService;
    private final AListService aListService;
    private final AliyunOpen aliyunOpen;
    private final UserRepository userRepository;

    /**
     * 获取授权二维码
     * 发送GET请求到/aliyun/drive/qr接口，用于获取授权二维码。
     *
     * @return JsonNode类型的返回结果
     */
    @GetMapping("/aliyun/drive/qr")
    public JsonNode drive_qr() {
        Response response = http.get(Payload.create("https://api.nn.ci/alist/ali/qr"));
        JsonNode jsonNode = response.asJsonNode();
        if (jsonNode.findValue("hasError").asBoolean()) {
            throw new ServiceException("获取授权二维码失败");
        }
        return jsonNode.findPath("data");
    }

    /**
     * 接收POST请求，用于获取阿里云驱动的授权二维码
     *
     * @param body 请求体参数
     * @return 返回授权二维码相关数据的JSON节点
     */
    @PostMapping("/aliyun/drive/qr")
    public JsonNode drive_qr_auth(@RequestBody Map<String, Object> body) {
        Payload payload = Payload.create("https://api.nn.ci/alist/ali/ck");
        payload.setBody(body);
        payload.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        Response response = http.post(payload);
        JsonNode jsonNode = response.asJsonNode();
        if (jsonNode.findValue("hasError").asBoolean(false)) {
            throw new ServiceException("获取授权二维码失败");
        }
        return jsonNode.findPath("data");
    }


    /**
     * 通过获取阿里云的授权二维码
     * 使用GET方法请求路径为"/aliyun/openapi/qr"
     *
     * @return 授权二维码的URL
     * @throws ServiceException 如果获取授权二维码失败，抛出ServiceException
     */
    @GetMapping("/aliyun/openapi/qr")
    public String openapi_qr() {
        Response response = http.post(Payload.create("https://api.nn.ci/alist/ali_open/qr"));
        JsonNode jsonNode = response.asJsonNode();
        String qr = jsonNode.findValue("qrCodeUrl").asText("");
        if (StringUtils.hasText(qr)) {
            return qr;
        }
        throw new ServiceException("获取授权二维码失败");
    }

    /**
     * 接收阿里云OpenAPI的授权请求
     *
     * @param body 请求参数，包含url字段表示请求的url
     * @return 返回授权后的refresh_token
     * @throws ServiceException 当授权失败时抛出异常
     */
    @PostMapping("/aliyun/openapi/qr")
    public String openapi_qr_auth(@RequestBody Map<String, String> body) throws ServiceException {
        String url = body.get("url");
        Response response = http.get(Payload.create("https://api.nn.ci/proxy/" + url));
        JsonNode jsonNode = response.asJsonNode();
        // 获取authCode字段的值，若不存在则为空字符串
        String authCode = jsonNode.findValue("authCode").asText("");
        // 如果authCode非空，则进行进一步处理
        if (StringUtils.hasText(authCode)) {
            // 创建请求Payload，并添加授权代码和其他参数
            Payload payload = Payload.create("https://api.nn.ci/alist/ali_open/code");
            payload.addBody("code", authCode);
            payload.addBody("grant_type", "authorization_code");
            // 发送POST请求获取新的refresh_token
            Response res = http.post(payload);
            // 将返回结果转换为JsonNode对象
            JsonNode node = res.asJsonNode();
            // 获取refresh_token字段的值，若不存在则为空字符串
            String refresh_token = node.findPath("refresh_token").asText("");
            // 如果refresh_token非空，则返回该值
            if (StringUtils.hasText(refresh_token)) {
                return refresh_token;
            }
        }
        // 如果授权失败，则抛出ServiceException异常
        throw new ServiceException("获取授权失败,请稍后重试");
    }


    /**
     * 初始化操作
     *
     * @param initializeDTO 初始化数据传输对象
     */
    @PostMapping("/initialize")
    @SneakyThrows
    public void initialize(@RequestBody @Valid InitializeDTO initializeDTO) {
        // 检查是否已经初始化
        if (!appConfigService.isInitialized()) {
            // 创建阿里云驱动实例
            AliyunDriveBO aliyunDriveBO = new AliyunDriveBO();
            aliyunDriveBO.setRefresh_token(initializeDTO.getDrive_refresh_token());
            // 保存或更新配置信息
            appConfigService.saveOrUpdate(aliyunDriveBO);

            // 创建阿里云应用实例
            AliyunOpenBO aliyunOpenBO = new AliyunOpenBO();
            aliyunOpenBO.setRefresh_token(initializeDTO.getOpen_refresh_token());
            // 保存或更新配置信息
            appConfigService.saveOrUpdate(aliyunOpenBO);

            // 在根目录创建文件夹
            Optional<FileInfo> fileInfo = aliyunOpen.createFile(Constants.FILE_NAME, "folder", "root");
            // 检查文件夹创建是否成功
            if (fileInfo.isEmpty()) {
                throw new ServiceException("创建文件夹失败");
            }

            // 创建阿里云文件夹对象
            AliyunFolderBo aliyunFolderBo = new AliyunFolderBo();
            aliyunFolderBo.setName(fileInfo.get().getFile_name());
            aliyunFolderBo.setFolder_id(fileInfo.get().getFile_id());
            aliyunFolderBo.setDrive_id(fileInfo.get().getDrive_id());
            // 保存或更新配置信息
            appConfigService.saveOrUpdate(aliyunOpenBO);

            // 新建线程执行初始化操作
            new Thread(() -> {
                try {
                    Thread.sleep(1000);  // 休眠1秒
                    // 初始化列表服务
                    aListService.initialize();
                    // 更新admin用户密码
                    User user = userRepository.findByName("admin");
                    user.setPassword(initializeDTO.getPassword());
                    userRepository.save(user);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);  // 记录异常信息
                }
            }).start();  // 启动线程
        }
    }
}

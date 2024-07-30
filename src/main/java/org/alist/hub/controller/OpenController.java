package org.alist.hub.controller;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.Constants;
import org.alist.hub.client.Http;
import org.alist.hub.client.Payload;
import org.alist.hub.client.Response;
import org.alist.hub.dto.InitializeDTO;
import org.alist.hub.exception.ServiceException;
import org.alist.hub.model.User;
import org.alist.hub.service.AListService;
import org.alist.hub.service.AliYunDriveService;
import org.alist.hub.service.AliYunOpenService;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.service.UserService;
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
    private final AListService aListService;
    private final AppConfigService appConfigService;
    private final AliYunDriveService aliYunDriveService;
    private final AliYunOpenService aliYunOpenService;
    private final UserService userService;

    /**
     * 获取授权二维码
     * 发送GET请求到/aliyun/drive/qr接口，用于获取授权二维码。
     *
     * @return JsonNode类型的返回结果
     */
    @GetMapping("/aliyun/drive/qr")
    public JsonNode getDriveQR() {
        Response response = Http.get(Payload.create(Constants.API_DOMAIN + "/alist/ali/qr"));
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
     */
    @PostMapping("/aliyun/drive/qr")
    public String authDriveQR(@RequestBody Map<String, Object> body) {
        if (appConfigService.isInitialized()) {
            throw new ServiceException("已经初始化过");
        }
        return aliYunDriveService.authorize(body);
    }


    /**
     * 通过获取阿里云的授权二维码
     * 使用GET方法请求路径为"/aliyun/openapi/qr"
     *
     * @return 授权二维码的URL
     */
    @GetMapping("/aliyun/openapi/qr")
    public String getOpenapiQR() {
        Response response = Http.post(Payload.create(Constants.API_DOMAIN + "/alist/ali_open/qr"));
        JsonNode jsonNode = response.asJsonNode();
        String qr = jsonNode.findValue("qrCodeUrl") != null ? jsonNode.findValue("qrCodeUrl").asText() : null;
        if (StringUtils.hasText(qr)) {
            return qr;
        }
        throw new ServiceException("获取授权二维码失败");
    }

    /**
     * 接收阿里云OpenAPI的授权请求
     *
     * @param body 请求参数，包含url字段表示请求的url
     */
    @PostMapping("/aliyun/openapi/qr")
    public void authOpenapiQR(@RequestBody Map<String, String> body) {
        if (appConfigService.isInitialized()) {
            throw new ServiceException("已经初始化过");
        }
        String url = body.get("url");
        aliYunOpenService.authorize(url);
    }


    /**
     * 初始化操作
     *
     * @param initializeDTO 初始化数据传输对象
     */
    @PostMapping("/initialize")
    @SneakyThrows
    public void initialize(@RequestBody @Valid InitializeDTO initializeDTO) {
        if (appConfigService.isInitialized()) {
            throw new ServiceException("已经初始化过");
        }
        if (!aListService.checkUpdate()) {
            throw new ServiceException("从服务器下载更新文件失败, 请检查服务器网络是否通畅");
        }
        appConfigService.initialize();
        Optional<User> user = userService.findByUsername("admin");
        user.map(u -> {
            u.setDisabled(0);
            u.setPassword(initializeDTO.getPassword());
            return userService.save(u);
        });
        aListService.update();

    }
}

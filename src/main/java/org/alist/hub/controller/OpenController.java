package org.alist.hub.controller;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.api.Http;
import org.alist.hub.api.Payload;
import org.alist.hub.bean.Response;
import org.alist.hub.dto.InitializeDTO;
import org.alist.hub.exception.ServiceException;
import org.alist.hub.service.AListService;
import org.alist.hub.service.AliYunDriveService;
import org.alist.hub.service.AliYunOpenService;
import org.alist.hub.service.AppConfigService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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
    private final AliYunDriveService aliYunDriveService;
    private final AliYunOpenService aliYunOpenService;

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
     */
    @PostMapping("/aliyun/drive/qr")
    public String drive_qr_auth(@RequestBody Map<String, Object> body) {
        return aliYunDriveService.authorize(body);
    }


    /**
     * 通过获取阿里云的授权二维码
     * 使用GET方法请求路径为"/aliyun/openapi/qr"
     *
     * @return 授权二维码的URL
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
     */
    @PostMapping("/aliyun/openapi/qr")
    public void openapi_qr_auth(@RequestBody Map<String, String> body) {
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
        // 检查是否已经初始化
        if (!appConfigService.isInitialized()) {
            // 新建线程执行初始化操作
            new Thread(() -> {
                try {
                    Thread.sleep(1000);  // 休眠1秒
                    aListService.initialize(initializeDTO.getPassword());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);  // 记录异常信息
                }
            }).start();  // 启动线程
        }
    }
}

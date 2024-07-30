package org.alist.hub.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.alist.hub.bean.Constants;
import org.alist.hub.bo.AliYunDriveBO;
import org.alist.hub.client.Http;
import org.alist.hub.client.Payload;
import org.alist.hub.client.Response;
import org.alist.hub.exception.ServiceException;
import org.alist.hub.service.AliYunDriveService;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.util.JsonUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AliYunDriveServiceImpl implements AliYunDriveService {
    private final AppConfigService appConfigService;

    @Override
    public String authorize(Map<String, Object> params) {
        // 创建请求的负载
        Payload payload = Payload.create(Constants.API_DOMAIN + "/alist/ali/ck");
        payload.setBody(params);
        payload.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        // 发送HTTP POST请求
        Response response = Http.post(payload);

        // 将响应转换为JsonNode对象
        JsonNode jsonNode = response.asJsonNode();

        // 获取bizExt字段，并判断是否为空
        Optional<JsonNode> bizExt = JsonUtils.getNodeByPath(jsonNode, "content.data.bizExt");
        if (bizExt.isEmpty()) {
            return JsonUtils.getNodeByPath(jsonNode, "content.data.qrCodeStatus").map(JsonNode::asText).orElse("授权失败");
        }

        // 解码bizExt字段的Base64编码
        byte[] bytes = Base64.getDecoder().decode(bizExt.get().asText());

        // 将解码后的字节数据转换为UTF-8编码的字符串
        String json = new String(bytes, StandardCharsets.UTF_8);

        // 将JSON字符串解析为AliYunDriveResp对象，判断是否解析成功
        Optional<AliYunDriveBO> resp = JsonUtils.readValue(json, AliYunDriveBO.class, "pds_login_result");
        if (resp.isEmpty()) {
            throw new ServiceException("解析数据失败");
        }

        // 更新ExpiresIn字段的值
        resp.get().setExpiresIn(System.currentTimeMillis() + (resp.get().getExpiresIn() - 300) * 1000);

        // 保存或更新aliYunDriveBO对象，抛出异常如果保存失败
        appConfigService.saveOrUpdate(resp.get());
        return "CONFIRMED";
    }

}

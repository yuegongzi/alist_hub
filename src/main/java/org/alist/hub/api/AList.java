package org.alist.hub.api;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Resource;
import org.alist.hub.utils.JsonUtil;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class AList {
    @Resource
    private RestTemplateBuilder builder;
    private RestTemplate restTemplate;

    private RestTemplate getRestTemplate() {
        synchronized (this) {
            if (restTemplate == null) {
                restTemplate = builder.rootUri("http://localhost:5244/api").build();
            }
        }
        return this.restTemplate;
    }

    // 判断接口返回结果是否成功
    private JsonNode isSuccess(ResponseEntity<String> response) {
        // 如果状态码不是2xx成功的范围
        if (!response.getStatusCode().is2xxSuccessful()) {
            // 抛出运行时异常，并附带错误信息
            throw new RuntimeException("AList Service Unavailable");
        }
        // 读取接口返回的JSON数据并转化为JsonNode对象
        JsonNode body = JsonUtil.readTree(response.getBody());
        // 从JSON数据中获取返回码
        int code = body.findValue("code").asInt();
        // 如果返回码为200
        if (code == 200) {
            // 返回数据部分
            return body.findValue("data");
        }
        // 如果返回码不为200，抛出异常，并使用错误信息作为异常描述
        throw new IllegalArgumentException(body.findValue("message").asText());
    }

    /**
     * 进行身份验证
     *
     * @param username 用户名
     * @param password 密码
     * @return 身份验证结果
     * @throws RuntimeException 当 AList Service不可用时抛出异常
     */
    public String auth(String username, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = getRestTemplate().exchange("/auth/login/hash", HttpMethod.POST, entity, String.class);
        JsonNode jsonNode = isSuccess(response);
        return jsonNode.findValue("token").asText();
    }

//    public String execute(String path, HttpMethod method, String body) {
//        return getRestTemplate().execute(path,method,)
//    }
}

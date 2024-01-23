package org.alist.hub.api;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.Constants;
import org.alist.hub.bean.Response;
import org.alist.hub.exception.ServiceException;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class AList {
    private final Http http;

    // 判断接口返回结果是否成功
    private JsonNode isSuccess(JsonNode body) {
        try {
            // 从JSON数据中获取返回码
            int code = body.findValue("code").asInt();
            // 如果返回码为200
            if (code == 200) {
                // 返回数据部分
                return body.findValue("data");
            }
            // 如果返回码不为200，抛出异常，并使用错误信息作为异常描述
            throw new ServiceException(body.findValue("message").asText());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException("service is not available ");
        }

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
        Payload payload = Payload.create(Constants.ALIST_BASE_URL + "/auth/login/hash")
                .addBody("username", username)
                .addBody("password", password);
        Response response = http.post(payload);
        JsonNode jsonNode = isSuccess(response.asJsonNode());
        return jsonNode.findValue("token").asText();
    }
}

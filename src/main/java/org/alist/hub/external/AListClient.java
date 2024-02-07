package org.alist.hub.external;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.Constants;
import org.alist.hub.bean.ExpiringMap;
import org.alist.hub.bean.FileSystem;
import org.alist.hub.bean.FileSystemResp;
import org.alist.hub.bean.Response;
import org.alist.hub.client.Http;
import org.alist.hub.client.Payload;
import org.alist.hub.exception.ServiceException;
import org.alist.hub.model.Storage;
import org.alist.hub.model.User;
import org.alist.hub.repository.UserRepository;
import org.alist.hub.util.JsonUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
public class AListClient {
    private final Http http;
    private final static ExpiringMap<String, String> expiringMap = new ExpiringMap<>();
    private final UserRepository userRepository;

    // 判断接口返回结果是否成功
    private JsonNode isSuccess(JsonNode body) {
        // 从JSON数据中获取返回码
        int code = body.findValue("code").asInt();
        // 如果返回码为200
        if (code == 200) {
            // 返回数据部分
            return body.findValue("data");
        }
        // 如果返回码不为200，抛出异常，并使用错误信息作为异常描述
        throw new ServiceException(body.findValue("message").asText());
    }

    private Payload create(String path) {
        String token = expiringMap.get("token");
        if (token == null) {
            Optional<User> user = userRepository.findByUsername("admin");
            if (user.isEmpty()) {
                throw new ServiceException("账户不存在");
            }
            token = this.auth(user.get().getUsername(), user.get().getPassword());
            expiringMap.put("token", token, 7200000);
        }
        return Payload.create(Constants.ALIST_BASE_URL + path).addHeader("Authorization", token);

    }

    /**
     * 重新启用指定id的存储
     *
     * @param id 存储的id
     */
    public void enable(Long id) {
        isSuccess(http.post(create("/admin/storage/enable").addParam("id", id.toString())).asJsonNode());
    }


    /**
     * 禁用指定id的存储
     *
     * @param id 存储的id
     */
    public void disable(Long id) {
        isSuccess(http.post(create("/admin/storage/disable").addParam("id", id.toString())).asJsonNode());
    }


    public void addOrUpdate(Storage storage) {
        String path = storage.getId() == null ? "create" : "update";
        Payload payload = create("/admin/storage/" + path);
        payload.addBody("mount_path", storage.getMountPath());
        payload.addBody("order", storage.getOrder());
        if (storage.getId() != null) {
            payload.addBody("id", storage.getId());
        }
        payload.addBody("remark", storage.getRemark());
        payload.addBody("cache_expiration", storage.getCacheExpiration());
        payload.addBody("web_proxy", false);
        payload.addBody("webdav_policy", storage.getWebdavPolicy());
        payload.addBody("down_proxy_url", storage.getDownProxyUrl());
        payload.addBody("extract_folder", storage.getExtractFolder());
        payload.addBody("driver", storage.getDriver());
        payload.addBody("order_by", storage.getOrderBy());
        payload.addBody("order_direction", storage.getOrderDirection());
        payload.addBody("addition", JsonUtils.toJson(storage.getAddition()));
        isSuccess(http.post(payload).asJsonNode());
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
        Payload payload = Payload.create(Constants.ALIST_BASE_URL + "/auth/login")
                .addBody("username", username)
                .addBody("password", password);
        Response response = http.post(payload);
        JsonNode jsonNode = isSuccess(response.asJsonNode());
        return jsonNode.findValue("token").asText();
    }

    private int calculateTotalPages(int total) {
        // 计算总页数
        int totalPages = total / 50;

        // 如果总记录数不能被每页记录数整除（有余数），说明还需要额外一页来展示剩余记录
        if (total % 50 != 0) {
            totalPages++;
        }

        return totalPages;
    }

    public List<FileSystem> fs(String path) {
        List<FileSystem> list = new ArrayList<>();
        fsExecute(path, list, 1);
        return list;
    }

    public void fsExecute(String path, List<FileSystem> list, int page) {
        JsonNode jsonNode = http.post(create("/fs/list")
                        .addBody("path", path)
                        .addBody("page", page)
                        .addBody("password", "")
                        .addBody("per_page", 50))
                .asJsonNode();
        if (jsonNode.findValue("code").asInt() == 200) {
            Optional<FileSystemResp> resp = JsonUtils.toPojo(jsonNode.findValue("data"), FileSystemResp.class);
            resp.ifPresent(r -> {
                if (r.getContent() != null) {
                    list.addAll(r.getContent());
                    if (calculateTotalPages(r.getTotal()) > page) {
                        fsExecute(path, list, page + 1);
                    }
                }

            });
        }
    }
}

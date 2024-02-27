package org.alist.hub.external;

import lombok.AllArgsConstructor;
import org.alist.hub.bo.Aria2BO;
import org.alist.hub.client.Http;
import org.alist.hub.client.Payload;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.util.RandomUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@AllArgsConstructor
public class Aria2Client {
    private final Http http;
    private final AppConfigService appConfigService;

    /**
     * 添加下载任务
     *
     * @param downloadUrl 下载链接
     * @param name        文件名
     */
    public void add(String downloadUrl, String name) {
        // 获取aria2配置信息
        Optional<Aria2BO> aria2BO = appConfigService.get(new Aria2BO(), Aria2BO.class);
        if (aria2BO.isPresent()) {
            // 构建参数列表
            List<Object> params = new ArrayList<>();
            Map<String, Object> out = new HashMap<>();
            out.put("out", name);
            out.put("check-certificate", false);
            params.add("token:" + aria2BO.get().getSecretKey());
            params.add(new String[]{downloadUrl});
            params.add(out);
            // 构建Payload
            Payload payload = Payload.create(aria2BO.get().getUrl())
                    .addBody("jsonrpc", "2.0")
                    .addBody("id", RandomUtils.generateRandomId())
                    .addBody("method", "aria2.addUri")
                    .addBody("params", params);
            // 发送POST请求
            http.post(payload);
        }
    }

    /**
     * 清除已完成的任务
     */
    public void clear() {
        // 获取aria2配置信息
        Optional<Aria2BO> aria2BO = appConfigService.get(new Aria2BO(), Aria2BO.class);
        if (aria2BO.isPresent()) {
            // 构建参数列表
            List<String> params = new ArrayList<>();
            params.add("token:" + aria2BO.get().getSecretKey());
            // 构建Payload
            Payload payload = Payload.create(aria2BO.get().getUrl());
            payload.addBody("jsonrpc", "2.0")
                    .addBody("id", RandomUtils.generateRandomId())
                    .addBody("method", "aria2.purgeDownloadResult")
                    .addBody("params", params);
            http.post(payload);
        }
    }
}

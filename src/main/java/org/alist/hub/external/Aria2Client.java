package org.alist.hub.external;

import lombok.AllArgsConstructor;
import org.alist.hub.bean.DownloadInfo;
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
            Http.post(payload);
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
            Http.post(payload);
        }
    }

    /**
     * 获取当前活跃的下载信息列表。
     * 该方法不接受任何参数，返回当前正在活动的下载任务的信息列表。
     *
     * @return 返回一个包含活跃下载任务信息的列表。如果无法连接到aria2或配置不正确，则返回空列表。
     */
    public List<DownloadInfo> active() {
        // 尝试从应用配置中获取aria2的配置信息
        Optional<Aria2BO> aria2BO = appConfigService.get(new Aria2BO(), Aria2BO.class);
        if (aria2BO.isPresent()) {
            // 准备调用aria2 API所需的参数
            List<String> params = new ArrayList<>();
            params.add("token:" + aria2BO.get().getSecretKey()); // 添加认证token
            // 构建向aria2发送的HTTP请求
            Payload payload = Payload.create(aria2BO.get().getUrl()); // 设置请求URL
            payload.addBody("jsonrpc", "2.0") // JSON-RPC版本
                    .addBody("id", RandomUtils.generateRandomId()) // 随机生成请求ID
                    .addBody("method", "aria2.tellActive") // 要调用的aria2方法
                    .addBody("params", params); // 请求参数
            // 发送HTTP POST请求并处理响应
            return Http.post(payload).asList(DownloadInfo.class, "result");
        }
        // 如果获取aria2配置失败，则返回空列表
        return new ArrayList<>();
    }

}

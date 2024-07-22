package org.alist.hub.external;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.client.HttpUtil;
import org.alist.hub.client.Payload;
import org.alist.hub.client.Response;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.util.JsonUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
public class QuarkClient {
    private final AppConfigService appConfigService;

    public double sign() {
        Map<String, Object> map = appConfigService.get("QuarkShare");
        if (map.isEmpty() || !map.containsKey("cookie")) {
            return 0d;
        }
        String cookie = map.get("cookie").toString();
        String referer = "https://pan.quark.cn";
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) quark-cloud-drive/2.5.20 Chrome/100.0.4896.160 Electron/18.3.5.4-b478491100 Safari/537.36 Channel/pckk_other_ch";
        String urlStr = "https://drive-m.quark.cn/1/clouddrive/capacity/growth/info?pr=ucpro&fr=pc&uc_param_str=";
        Payload payload = Payload.create(urlStr)
                .addHeader("Cookie", cookie)
                .addHeader("User-Agent", userAgent)
                .addHeader("Referer", referer);
        Response response = HttpUtil.get(payload);
        if (response.getStatusCode().is2xxSuccessful()) {
            JsonNode jsonNode = response.asJsonNode();
            Optional<JsonNode> result = JsonUtils.getNodeByPath(jsonNode, "data.cap_sign.sign_daily_reward");
            if (result.isPresent()) {
                return result.get().asDouble();
            }
        }
        return 0d;
    }
}

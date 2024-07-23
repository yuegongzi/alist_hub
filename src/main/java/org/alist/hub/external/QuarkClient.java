package org.alist.hub.external;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.client.HttpUtil;
import org.alist.hub.client.Payload;
import org.alist.hub.client.Response;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.util.JsonUtils;
import org.alist.hub.util.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
@AllArgsConstructor
@Slf4j
public class QuarkClient {
    private final AppConfigService appConfigService;

    private String getSignUrl() {
        Map<String, Object> map = appConfigService.get("QuarkShare");
        if (map.isEmpty() || !map.containsKey("signUrl")) {
            return null;
        }
        String signUrl = map.get("signUrl").toString();
        if (StringUtils.isUrl(signUrl)) {
            Pattern pattern = Pattern.compile("%[0-9A-Fa-f]{2}");
            if (pattern.matcher(signUrl).find()) {
                return URLDecoder.decode(signUrl, StandardCharsets.UTF_8);
            }
            return signUrl;
        }
        return null;
    }

    public MultiValueMap<String, String> getParams() {
        String url = getSignUrl();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        if (StringUtils.hasText(url)) {
            URI uri = URI.create(url);
            MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUri(uri).build().getQueryParams();
            params.put("pr", queryParams.get("pr"));
            params.put("fr", queryParams.get("fr"));
            params.put("kps", queryParams.get("kps"));
            params.put("sign", queryParams.get("sign"));
            params.put("vcode", queryParams.get("vcode"));
        }
        return params;
    }


    public double sign() {
        MultiValueMap<String, String> params = getParams();
        if (!params.isEmpty()) {
            double raw = signInfo(params);
            if (raw > 0d) {//今日已签到
                return raw;
            }
            String urlStr = "https://drive-m.quark.cn/1/clouddrive/capacity/growth/sign";
            Payload payload = Payload.create(urlStr)
                    .addBody("sign_cyclic", true);
            payload.setUrlParams(params);
            Response response = HttpUtil.post(payload);
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode jsonNode = response.asJsonNode();
                Optional<JsonNode> result = JsonUtils.getNodeByPath(jsonNode, "data.sign_daily_reward");
                if (result.isPresent()) {
                    return result.get().asDouble();
                }
            }
        }
        return 0d;
    }

    private double signInfo(MultiValueMap<String, String> params) {
        String urlStr = "https://drive-m.quark.cn/1/clouddrive/capacity/growth/info";
        Payload payload = Payload.create(urlStr);
        payload.setUrlParams(params);
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

package org.alist.hub.external;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.client.Http;
import org.alist.hub.client.Payload;
import org.alist.hub.client.Response;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.util.ByteUtils;
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

    private Optional<JsonNode> check(Response response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            JsonNode jsonNode = response.asJsonNode();
            int status = jsonNode.findValue("status").asInt();
            if (status == 200) {
                return Optional.of(jsonNode);
            }
        }
        return Optional.empty();
    }
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


    private Optional<JsonNode> sign(MultiValueMap<String, String> params) {
        String urlStr = "https://drive-m.quark.cn/1/clouddrive/capacity/growth/sign";
        Payload payload = Payload.create(urlStr)
                .addBody("sign_cyclic", true);
        payload.setUrlParams(params);
        Response response = Http.post(payload);
        return check(response);
    }

    public String signInfo() {
        MultiValueMap<String, String> params = getParams();
        if (params.isEmpty()) {
            return "未配置夸克签到链接";
        }
        String urlStr = "https://drive-m.quark.cn/1/clouddrive/capacity/growth/info";
        Payload payload = Payload.create(urlStr);
        payload.setUrlParams(params);
        Response response = Http.get(payload);
        Optional<JsonNode> check = check(response);
        if (check.isPresent()) {
            StringBuilder stringBuilder = new StringBuilder();
            JsonNode jsonNode = check.get().findValue("data");
            boolean vip = jsonNode.findPath("88VIP").asBoolean();
            if (vip) {
                stringBuilder.append("88VIP ");
            } else {
                stringBuilder.append("普通用户 ");
            }
            stringBuilder.append("\n网盘总容量：");
            stringBuilder.append(ByteUtils.convertBytes(jsonNode.findPath("total_capacity").asLong()));
            stringBuilder.append("\n签到累计容量：");
            Optional<JsonNode> sign_reward = JsonUtils.getNodeByPath(jsonNode, "cap_composition.sign_reward");
            if (sign_reward.isPresent()) {
                stringBuilder.append(ByteUtils.convertBytes(sign_reward.get().asLong()));
            } else {
                stringBuilder.append(ByteUtils.convertBytes(0));
            }
            Optional<JsonNode> sign_daily = JsonUtils.getNodeByPath(jsonNode, "cap_sign.sign_daily");
            if (sign_daily.isPresent()) {
                JsonNode cap_sign = jsonNode.findPath("cap_sign");
                stringBuilder.append("\n✅ 签到日志: 今日已签到 ");
                stringBuilder.append(ByteUtils.convertBytes(cap_sign.findPath("sign_daily_reward").asLong()));
                stringBuilder.append("\n连签进度: ");
                stringBuilder.append(cap_sign.findPath("sign_progress").asInt() / cap_sign.findPath("sign_target").asInt());
                return stringBuilder.toString();
            } else {
                Optional<JsonNode> sign_result = sign(params);
                if (sign_result.isEmpty()) {
                    return "签到失败";
                }
                return signInfo();
            }
        }
        return "签到失败";
    }
}

package org.alist.hub.external;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.Constants;
import org.alist.hub.client.Http;
import org.alist.hub.client.Payload;
import org.alist.hub.client.Response;
import org.alist.hub.model.AppConfig;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.util.JsonUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
public class UCClient {
    private final AppConfigService appConfigService;

    public boolean refreshCookie() {
        Map<String, Object> map = appConfigService.get("UCShare");
        if (map.isEmpty() || !map.containsKey("cookie")) {
            return false;
        }
        String cookie = map.get("cookie").toString();
        String referer = "https://drive.uc.cn";
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) quark-cloud-drive/2.5.20 Chrome/100.0.4896.160 Electron/18.3.5.4-b478491100 Safari/537.36 Channel/pckk_other_ch";
        String urlStr = "https://pc-api.uc.cn/1/clouddrive/file/sort?pr=UCBrowser&fr=pc&pdir_fid=0&_page=1&_size=50&_fetch_total=1&_fetch_sub_dirs=0&_sort=file_type:asc,updated_at:desc";
        Payload payload = Payload.create(urlStr)
                .addHeader("Cookie", cookie)
                .addHeader("User-Agent", userAgent)
                .addHeader("Referer", referer);
        Response response = Http.get(payload);
        if (response.getStatusCode().is4xxClientError()) {//401错误
            return false;
        }
        Optional<String> header = response.getHeaders().firstValue("Set-Cookie");
        if (header.isPresent()) {
            String newPuus = extractPuus(header.get());
            String newCookie = cookie.replaceFirst("__puus=[^;]*", newPuus);
            AppConfig appConfig = new AppConfig();
            map.put("cookie", newCookie);
            appConfig.setValue(JsonUtils.toJson(map));
            appConfig.setGroup(Constants.ALIST_GROUP);
            appConfig.setId("UCShare");
            appConfigService.save(appConfig);
            return true;
        }
        return false;
    }

    private String extractPuus(String setCookie) {
        int start = setCookie.indexOf("__puus=") + "__puus=".length();
        int end = setCookie.indexOf(';', start);
        return setCookie.substring(start, end);
    }
}

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

    public void add(String downloadUrl, String name) {
        Optional<Aria2BO> aria2BO = appConfigService.get(new Aria2BO(), Aria2BO.class);
        if (aria2BO.isPresent()) {
            List<Object> params = new ArrayList<>();
            Map<String, Object> out = new HashMap<>();
            out.put("out", name);
            out.put("check-certificate", false);
            params.add("token:" + aria2BO.get().getSecretKey());
            params.add(new String[]{downloadUrl});
            params.add(out);
            Payload payload = Payload.create(aria2BO.get().getUrl())
                    .addBody("jsonrpc", "2.0")
                    .addBody("id", RandomUtils.generateRandomId())
                    .addBody("method", "aria2.addUri")
                    .addBody("params", params);
            http.post(payload);
        }
    }
}

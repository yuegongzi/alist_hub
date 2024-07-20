package org.alist.hub.client;

import lombok.Getter;
import lombok.Setter;
import org.alist.hub.util.JsonUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Setter
@Getter
public class Payload {
    private String url;
    private Map<String, Object> body;
    private Map<String, String> headers;
    private MultiValueMap<String, String> urlParams;
    private MultiValueMap<String, String> form;

    public Payload(String url) {
        this.url = url;
    }

    public static Payload create(String url) {
        Payload payload = new Payload(url);
        payload.setBody(new HashMap<>());
        payload.setUrlParams(new LinkedMultiValueMap<>());
        payload.setHeaders(new HashMap<>());
        payload.setForm(new LinkedMultiValueMap<>());
        return payload;
    }

    public Payload addHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public Payload addBody(String key, Object value) {
        body.put(key, value);
        return this;
    }

    public Payload addParam(String key, String value) {
        urlParams.add(key, value);
        return this;
    }

    public Payload addForm(String key, String value) {
        this.form.add(key, value);
        return this;
    }

    public URI getUri() {
        return UriComponentsBuilder.fromUriString(this.url)
                .queryParams(this.urlParams)
                .build(false)
                .toUri();
    }

    public String getBodyString() {
        if (this.form.isEmpty()) {
            this.addHeader("Content-Type", "application/json");
            return JsonUtils.toJson(this.body);
        }
        this.addHeader("Content-Type", "application/x-www-form-urlencoded");
        return this.form.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }

}

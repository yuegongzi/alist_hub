package org.alist.hub.api;

import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Data
public class Payload {
    private String url;
    private Map<String, Object> body;
    private HttpHeaders headers;
    private MultiValueMap<String, String> urlParams;

    public Payload(String url) {
        this.url = url;
    }

    public static Payload create(String url) {
        Payload payload = new Payload(url);
        payload.setBody(new HashMap<>());
        payload.setUrlParams(new LinkedMultiValueMap<>());
        payload.setHeaders(new HttpHeaders());
        return payload;
    }

    public Payload addHeader(String key, String value) {
        headers.add(key, value);
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

    public URI getUri() {
        return UriComponentsBuilder.fromHttpUrl(this.url)
                .queryParams(this.urlParams)
                .build(true)
                .toUri();
    }

}

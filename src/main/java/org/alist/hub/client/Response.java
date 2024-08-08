package org.alist.hub.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.util.JsonUtils;
import org.springframework.http.HttpStatusCode;

import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

@Slf4j
public record Response(HttpResponse<String> httpResponse) {

    public static Response of(HttpResponse<String> httpResponse) {
        return new Response(httpResponse);
    }

    public JsonNode asJsonNode() {
        return JsonUtils.readTree(this.httpResponse.body());
    }

    public <T> Optional<T> asValue(Class<T> classValue, String path) {
        return JsonUtils.readValue(this.httpResponse.body(), classValue, path);
    }

    public <T> Optional<T> asValue(Class<T> classValue) {
        return JsonUtils.readValue(this.httpResponse.body(), classValue);
    }

    public <T> List<T> asList(Class<T> classValue, String path) {
        return JsonUtils.readTreeValue(this.httpResponse.body(), classValue, path);
    }

    public HttpStatusCode getStatusCode() {
        return HttpStatusCode.valueOf(this.httpResponse.statusCode());
    }

    public HttpHeaders getHeaders() {
        return this.httpResponse.headers();
    }
}

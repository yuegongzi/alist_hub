package org.alist.hub.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.util.JsonUtils;
import org.springframework.http.HttpStatusCode;

import java.util.List;
import java.util.Optional;

@Slf4j
public record Response(int statusCode, String body) {

    public static Response of(int statusCode, String body) {
        return new Response(statusCode, body);
    }

    public JsonNode asJsonNode() {
        return JsonUtils.readTree(this.body);
    }

    public <T> Optional<T> asValue(Class<T> classValue, String path) {
        return JsonUtils.readValue(this.body, classValue, path);
    }

    public <T> Optional<T> asValue(Class<T> classValue) {
        return JsonUtils.readValue(this.body, classValue);
    }

    public <T> List<T> asList(Class<T> classValue, String path) {
        return JsonUtils.readTreeValue(this.body, classValue, path);
    }

    public HttpStatusCode getStatusCode() {
        return HttpStatusCode.valueOf(this.statusCode);
    }
}

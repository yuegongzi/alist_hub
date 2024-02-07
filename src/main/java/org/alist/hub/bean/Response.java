package org.alist.hub.bean;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.util.JsonUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@Slf4j
public class Response {
    private final ResponseEntity<String> responseEntity;

    public Response(ResponseEntity<String> responseEntity) {
        this.responseEntity = responseEntity;
    }

    public static Response of(ResponseEntity<String> responseEntity) {
        return new Response(responseEntity);
    }

    public HttpStatusCode getStatusCode() {
        return this.responseEntity.getStatusCode();
    }

    public String getBody() {
        return this.responseEntity.getBody();
    }

    public JsonNode asJsonNode() {
        return JsonUtils.readTree(this.getBody());
    }

    public <T> Optional<T> asValue(Class<T> classValue, String path) {
        return JsonUtils.readValue(this.getBody(), classValue, path);
    }

    public <T> Optional<T> asValue(Class<T> classValue) {
        return JsonUtils.readValue(this.getBody(), classValue);
    }

    public <T> List<T> asList(Class<T> classValue, String path) {
        return JsonUtils.readTreeValue(this.getBody(), classValue, path);
    }
}

package org.alist.hub.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.util.JsonUtils;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@Slf4j
public record Response(ResponseEntity<String> responseEntity) {

    public static Response of(ResponseEntity<String> responseEntity) {
        return new Response(responseEntity);
    }

    public JsonNode asJsonNode() {
        return JsonUtils.readTree(this.getBody());
    }

    public String getBody() {
        return this.responseEntity.getBody();
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

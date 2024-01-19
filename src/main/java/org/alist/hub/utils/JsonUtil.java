package org.alist.hub.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Objects;

public class JsonUtil {

    /**
     * Converts a JSON string into a JsonNode object.
     *
     * @param json the JSON string to be converted
     * @return the JsonNode representing the JSON structure
     * @throws IllegalArgumentException if the JSON format is illegal
     */
    public static JsonNode readTree(String json) {
        Objects.requireNonNull(json, "JSON string cannot be null");
        try {
            return getInstance().readTree(json);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse JSON string: " + e.getMessage(), e);
        }
    }

    public static <T> String toJson(T value) {
        try {
            return getInstance().writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed  convert to JSON string: " + e.getMessage(), e);
        }
    }


    private static ObjectMapper getInstance() {
        // Implementation of the single instance creation
        return new ObjectMapper();
    }


}

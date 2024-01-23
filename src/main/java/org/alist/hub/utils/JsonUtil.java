package org.alist.hub.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Optional;
import java.util.TimeZone;

@Slf4j
@SuppressWarnings("deprecation")
public class JsonUtil {

    private static final JsonMapper jsonMapper = new JsonMapper();

    static {
        // 去掉默认的时间戳格式
        jsonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // 设置为中国上海时区
        jsonMapper.setTimeZone(TimeZone.getTimeZone(ZoneId.SHORT_IDS.get("CTT")));

        // 序列化时，日期的统一格式为 'yyyy-MM-dd HH:mm:ss'
        jsonMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        // 允许JSON字符串包含单引号
        jsonMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        // 允许JSON字符串包含非引号控制字符
        jsonMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);

        // 允许反斜杠转义任意字符
        jsonMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);

        // 失败处理：序列化空Bean时不抛出异常
        jsonMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // 反序列化时忽略未知属性
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 注册Java 8时间模块以支持Java 8日期时间类的序列化与反序列化
        jsonMapper.registerModule(new JavaTimeModule());
    }

    public static JsonNode readTree(String json) {
        try {
            return jsonMapper.readTree(json);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Json parsing failed");
        }
    }

    public static <T> String toJson(T object) {
        try {
            return jsonMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            return "{}";
        }
    }

    public static <T> Optional<T> readValue(String json, Class<T> valueType) {
        try {
            return Optional.of(jsonMapper.readValue(json, valueType));
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }
}

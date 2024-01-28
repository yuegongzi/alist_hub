package org.alist.hub.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public static Map<String, Object> toMap(String json) {
        try {
            return jsonMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            return new HashMap<>();
        }
    }


    /**
     * 根据路径keyPath从JsonNode node中获取JsonNode节点
     *
     * @param node    JsonNode节点
     * @param keyPath 节点路径 形如a.b.c.d形式
     * @return 返回指定路径的JsonNode节点，如果路径不存在则返回空
     */
    public static Optional<JsonNode> getNodeByPath(JsonNode node, String keyPath) {
        String[] keys = keyPath.split("\\.");
        for (String key : keys) {
            node = node.path(key);
            if (node.isMissingNode()) {
                return Optional.empty(); // 任意节点为空则返回空
            }
        }
        return Optional.of(node);
    }

    /**
     * 将JSON字符串转换为指定类型的List对象
     *
     * @param json  JSON格式的字符串
     * @param clazz 需要转换的目标元素类型
     * @param <T>   泛型，表示列表中元素的类型
     * @return 转换后的List<T>对象，如果转换失败则返回空Optional
     */
    public static <T> Optional<List<T>> readTreeValue(String json, Class<T> clazz) {
        try {
            return Optional.of(jsonMapper.readValue(json, jsonMapper.getTypeFactory().constructCollectionType(List.class, clazz)));
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 将JsonNode转换为指定类型的实例对象
     *
     * @param node  JsonNode对象
     * @param clazz 目标对象的Class类型
     * @param <T>   目标对象的类型
     * @return 目标对象的Optional实例，如果转换失败则返回空Optional
     */
    public static <T> Optional<T> jsonNodeToObject(JsonNode node, Class<T> clazz) {
        try {
            return Optional.of(jsonMapper.treeToValue(node, clazz));
        } catch (JsonProcessingException e) {
            log.error("Failed to convert JsonNode to object of type '{}': {}", clazz.getName(), e.getMessage(), e);
            return Optional.empty();
        }
    }
}

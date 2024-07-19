package org.alist.hub.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

@Slf4j
@SuppressWarnings("deprecation")
public class JsonUtils {

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

    /**
     * 读取JSON字符串并返回对应的JsonNode对象
     *
     * @param json JSON字符串
     * @return JsonNode对象
     * @throws RuntimeException JSON解析失败时抛出异常
     */
    public static JsonNode readTree(String json) {
        try {
            return jsonMapper.readTree(json);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Json parsing failed");
        }
    }

    /**
     * 将指定对象转换为JSON字符串
     *
     * @param object 要转换的对象
     * @param <T>    对象的类型
     * @return 转换后的JSON字符串
     */
    public static <T> String toJson(T object) {
        try {
            return jsonMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            return "{}";
        }
    }

    /**
     * 从JSON字符串中读取指定类型的值
     *
     * @param json      JSON字符串
     * @param valueType 指定的类型
     * @param <T>       指定的类型
     * @return 读取到的值的Optional对象
     */
    public static <T> Optional<T> readValue(String json, Class<T> valueType) {
        try {
            return Optional.of(jsonMapper.readValue(json, valueType));
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    public static <T> Optional<T> readValue(String json, Class<T> classValue, String path) {
        JsonNode jsonNode = readTree(json);
        Optional<JsonNode> value = getNodeByPath(jsonNode, path);
        if (value.isPresent()) {
            return toPojo(value.get(), classValue);
        }
        return Optional.empty();
    }


    /**
     * 将JSON字符串转换为Map对象
     *
     * @param json JSON字符串
     * @return 转换后的Map对象
     */
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
     * 从JSON字符串中读取指定路径的值，并将其转换为指定类型的列表
     *
     * @param json  JSON字符串
     * @param clazz 指定的类型
     * @param path  要读取的路径
     * @param <T>   指定的类型
     * @return 读取到的值的列表
     */
    public static <T> List<T> readTreeValue(String json, Class<T> clazz, String path) {
        JsonNode dataNode = readTree(json);
        Optional<JsonNode> value = getNodeByPath(dataNode, path);
        List<T> resultList = new ArrayList<>();
        if (value.isPresent()) {
            JsonNode jsonNode = value.get();
            if (jsonNode.isArray()) {
                Iterator<JsonNode> elements = jsonNode.elements();
                while (elements.hasNext()) {
                    JsonNode element = elements.next();
                    try {
                        T obj = jsonMapper.treeToValue(element, clazz);
                        resultList.add(obj);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to convert JsonNode to object of type '{}': {}", clazz.getName(), e.getMessage(), e);
                    }
                }
            }
        }
        return resultList;
    }

    /**
     * 将JsonNode转换为指定类型的实例对象
     *
     * @param node  JsonNode对象
     * @param clazz 目标对象的Class类型
     * @param <T>   目标对象的类型
     * @return 目标对象的Optional实例，如果转换失败则返回空Optional
     */
    public static <T> Optional<T> toPojo(JsonNode node, Class<T> clazz) {
        try {
            return Optional.of(jsonMapper.treeToValue(node, clazz));
        } catch (JsonProcessingException e) {
            log.error("Failed to convert JsonNode to object of type '{}': {}", clazz.getName(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 合并两个JSON节点。
     * <p>
     * 此方法用于将updateNode中的所有字段合并到mainNode中。如果mainNode中已存在某个字段，
     * 且该字段在两个节点中都是对象，则递归合并这两个对象。否则，用updateNode中的值覆盖mainNode中的值。
     * 注意，此方法只处理字段的合并，并不处理数组类型的合并。
     *
     * @param mainNode   原始JSON节点，将被updateNode中的字段合并。
     * @param updateNode 更新用的JSON节点，其字段将被合并到mainNode中。
     * @return 合并后的JSON节点，即mainNode经过更新后的结果。
     */

    public static JsonNode mergeJsonNodes(JsonNode mainNode, JsonNode updateNode) {
        // 遍历 updateNode 的字段
        updateNode.fieldNames().forEachRemaining(fieldName -> {
            JsonNode value = updateNode.get(fieldName);
            // 如果 updateNode 中的字段有值（存在该字段），则覆盖 mainNode 中的值
            if (value != null && !value.isNull()) {
                // 如果 mainNode 中的值是一个对象并且 updateNode 中的值也是一个对象，则递归合并
                if (mainNode.has(fieldName) && mainNode.get(fieldName).isObject() && value.isObject()) {
                    mergeJsonNodes(mainNode.get(fieldName), value);
                } else {
                    // 否则，使用 updateNode 的值覆盖 mainNode 的值
                    ((ObjectNode) mainNode).set(fieldName, value);
                }
            }
        });
        return mainNode;
    }
}

package net.sudot.sessionmanager.exampleapp.tomcatredis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * Jackson常用API工具
 *
 * @author tangjialin on 2018-07-02.
 */
public final class JsonUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        OBJECT_MAPPER.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * 不可实例化
     */
    private JsonUtils() {
    }

    /**
     * 将对象转换为JSON字符串
     *
     * @param value 对象
     * @return JSON字符串
     */
    public static String toJson(Object value) {
        if (value == null) { return null; }

        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 将对象转换为JSON流
     *
     * @param writer Writer
     * @param value  对象
     */
    public static void toJson(Writer writer, Object value) {
        try {
            OBJECT_MAPPER.writeValue(writer, value);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 将对象转换为JSON流
     *
     * @param stream OutputStream
     * @param value  对象
     */
    public static void toJson(OutputStream stream, Object value) {
        try {
            OBJECT_MAPPER.writeValue(stream, value);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 将对象转换为JSON字符串
     *
     * @param value 对象
     * @return JSON字符串
     */
    public static byte[] toJsonBytes(Object value) {
        if (value == null) { return null; }

        try {
            return OBJECT_MAPPER.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 将对象转换为JSON字符串并美化
     *
     * @param value 对象
     * @return JSON字符串
     */
    public static String toPrettyJson(Object value) {
        if (value == null) { return null; }

        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 将JSON字符串转换为JSON对象
     *
     * @param json JSON字符串
     * @return 对象
     */
    public static JsonNode toObject(String json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 将JSON字符串转换为对象
     *
     * @param json      JSON字符串
     * @param valueType 类型
     * @return 对象
     */
    public static <T> T toObject(String json, Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(json, valueType);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 将JSON字符串转换为对象
     * <pre>
     *     List<MyDto> asList = JsonUtils.toObject(jsonBytes, new TypeReference<List<MyDto>>() { });
     *     Map<String, Object> asMap = JsonUtils.toObject(jsonBytes, new TypeReference<Map<String, Object>>() { });
     * </pre>
     *
     * @param json         JSON字符串
     * @param valueTypeRef 类型
     * @return 对象
     */
    public static <T> T toObject(String json, TypeReference<T> valueTypeRef) {
        try {
            return OBJECT_MAPPER.readValue(json, valueTypeRef);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 将JSON字符串转换为JSON对象
     *
     * @param jsonBytes JSON字节数组
     * @return 对象
     */
    public static JsonNode toObject(byte[] jsonBytes) {
        try {
            return OBJECT_MAPPER.readTree(jsonBytes);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 将JSON字符串转换为对象
     *
     * @param jsonBytes JSON字节数组
     * @param valueType 类型
     * @return 对象
     */
    public static <T> T toObject(byte[] jsonBytes, Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(jsonBytes, valueType);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 将JSON字符串转换为对象
     * <pre>
     *     List<MyDto> asList = JsonUtils.toObject(jsonBytes, new TypeReference<List<MyDto>>() { });
     *     Map<String, Object> asMap = JsonUtils.toObject(jsonBytes, new TypeReference<Map<String, Object>>() { });
     * </pre>
     *
     * @param jsonBytes    JSON字节数组
     * @param valueTypeRef 类型
     * @return 对象
     */
    public static <T> T toObject(byte[] jsonBytes, TypeReference<T> valueTypeRef) {
        try {
            return OBJECT_MAPPER.readValue(jsonBytes, valueTypeRef);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 将JSON字符串转换为JSON对象
     *
     * @param inputStream JSON字符串输入流
     * @return 对象
     */
    public static JsonNode toObject(InputStream inputStream) {
        try {
            return OBJECT_MAPPER.readTree(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 将JSON字符串转换为对象
     *
     * @param inputStream JSON字符串输入流
     * @param valueType   类型
     * @return 对象
     */
    public static <T> T toObject(InputStream inputStream, Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(inputStream, valueType);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 将JSON字符串转换为对象
     * <pre>
     *     List<MyDto> asList = JsonUtils.toObject(jsonBytes, new TypeReference<List<MyDto>>() { });
     *     Map<String, Object> asMap = JsonUtils.toObject(jsonBytes, new TypeReference<Map<String, Object>>() { });
     * </pre>
     *
     * @param inputStream  JSON字符串输入流
     * @param valueTypeRef 类型
     * @return 对象
     */
    public static <T> T toObject(InputStream inputStream, TypeReference<T> valueTypeRef) {
        try {
            return OBJECT_MAPPER.readValue(inputStream, valueTypeRef);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 将Object对象转换为Json对象
     *
     * @param value Object对象
     * @return 对象
     */
    public static <T extends JsonNode> T toObject(Object value) {
        if (value == null) { return null; }
        return OBJECT_MAPPER.valueToTree(value);
    }

    /**
     * 将Object对象转换为指定类型对象
     *
     * @param value     Object对象
     * @param valueType 类型
     * @return 对象
     */
    public static <T> T toObject(Object value, Class<T> valueType) {
        if (value == null) { return null; }
        return OBJECT_MAPPER.convertValue(value, valueType);
    }

    /**
     * 将Object对象转换为指定类型对象
     * <pre>
     *     List<MyDto> asList = JsonUtils.toObject(value, new TypeReference<List<MyDto>>() { });
     *     Map<String, Object> asMap = JsonUtils.toObject(value, new TypeReference<Map<String, Object>>() { });
     * </pre>
     *
     * @param value        Object对象
     * @param valueTypeRef 类型
     * @return 对象
     */
    public static <T> T toObject(Object value, TypeReference<T> valueTypeRef) {
        return OBJECT_MAPPER.convertValue(value, valueTypeRef);
    }

    /**
     * 创建一个ObjectNode
     *
     * @return 返回ObjectNode
     */
    public static ObjectNode createObjectNode() {
        return OBJECT_MAPPER.createObjectNode();
    }

    /**
     * 创建一个ObjectNode
     *
     * @param kids 用于存放json数据的map
     * @return 返回ObjectNode
     */
    public static ObjectNode createObjectNode(Map<String, JsonNode> kids) {
        return new ObjectNode(OBJECT_MAPPER.getNodeFactory(), kids);
    }

    /**
     * 创建一个ArrayNode
     *
     * @return 返回ArrayNode
     */
    public static ArrayNode createArrayNode() {
        return OBJECT_MAPPER.createArrayNode();
    }

    /**
     * 创建一个ArrayNode
     *
     * @param children 用于存放json数据的list
     * @return 返回ArrayNode
     */
    public static ArrayNode createArrayNode(List<JsonNode> children) {
        return new ArrayNode(OBJECT_MAPPER.getNodeFactory(), children);
    }
}
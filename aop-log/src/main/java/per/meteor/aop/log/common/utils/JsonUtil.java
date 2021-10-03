package per.meteor.aop.log.common.utils;

import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author meteor
 * @date 2021-10-02 3:44
 */
@Slf4j
public class JsonUtil {
    /**
     *    加载速度太慢了，放在静态代码块中
     */
    private static final ObjectMapper mapper;

    private JsonUtil() {

    }
    /*
      设置一些通用的属性
     */
    static {
        mapper = new ObjectMapper();
        // 支持 jdk 1.8 日期   ---- start ---
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .registerModule(new ParameterNamesModule());
        // --end --
        // 如果存在未知属性，则忽略不报错
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 允许key没有双引号
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // 允许key有单引号
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    public static String toJSONString(Object obj) {
        return obj != null ? toJSONString(obj, () -> "", false) : "";
    }

    public static String toFormatJSONString(Object obj) {
        return obj != null ? toJSONString(obj, () -> "", true) : "";
    }


    public static String toJSONString(Object obj, Supplier<String> defaultSupplier, boolean format) {
        try {
            if (obj == null) {
                return defaultSupplier.get();
            }
            if (obj instanceof String) {
                return obj.toString();
            }
            if (obj instanceof Number) {
                return obj.toString();
            }
            if (format) {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            }
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error(String.format("toJSONString %s", obj != null ? obj.toString() : "null"), e);
        }
        return defaultSupplier.get();
    }

    public static <T> T toJavaObject(String value, Class<T> tClass) {
        return !ObjectUtils.isEmpty(value) ? toJavaObject(value, tClass, () -> null) : null;
    }

    public static <T> T toJavaObject(Object obj, Class<T> tClass) {
        return obj != null ? toJavaObject(toJSONString(obj), tClass, () -> null) : null;
    }

    public static <T> T toJavaObject(String value, Class<T> tClass, Supplier<T> defaultSupplier) {
        try {
            if (ObjectUtils.isEmpty(value)) {
                return defaultSupplier.get();
            }
            return mapper.readValue(value, tClass);
        } catch (Exception e) {
            log.error("toJavaObject exception: \n {}\n {}", value, tClass, e);
        }
        return defaultSupplier.get();
    }

    public static <T> List<T> toJavaObjectList(String value, Class<T> tClass) {
        return !ObjectUtils.isEmpty(value) ? toJavaObjectList(value, tClass, () -> null) : null;
    }

    public static <T> List<T> toJavaObjectList(Object obj, Class<T> tClass) {
        return obj != null ? toJavaObjectList(toJSONString(obj), tClass, () -> null) : null;
    }

    public static <T> List<T> toJavaObjectList(String value, Class<T> tClass, Supplier<List<T>> defaultSupplier) {
        try {
            if (ObjectUtils.isEmpty(value)) {
                return defaultSupplier.get();
            }
            JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, tClass);
            return mapper.readValue(value, javaType);
        } catch (Exception e) {
            log.error("toJavaObjectList exception: \n {}\n {}", value, tClass, e);
        }
        return defaultSupplier.get();
    }

    public static <T> T jsonCopy(Object obj, Class<T> tClass) {
        return obj != null ? toJavaObject(toJSONString(obj), tClass) : null;
    }

    public static Map<String, Object> toMap(String value) {
        return !ObjectUtils.isEmpty(value) ? toMap(value, () -> null) : null;
    }

    public static Map<String, Object> toMap(Object value) {
        return value != null ? toMap(value, () -> null) : null;
    }

    public static Map<String, Object> toMap(Object value, Supplier<Map<String, Object>> defaultSupplier) {
        if (value == null) {
            return defaultSupplier.get();
        }
        try {
            return mapper.readValue(toJSONString(value), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.info("fail to convert" + toJSONString(value), e);
        }
        return toMap(toJSONString(value), defaultSupplier);
    }

    public static Map<String, Object> toMap(String value, Supplier<Map<String, Object>> defaultSupplier) {
        if (ObjectUtils.isEmpty(value)) {
            return defaultSupplier.get();
        }
        try {
            return mapper.readValue(value, new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (Exception e) {
            log.error("toMap exception\n{}", value, e);
        }
        return defaultSupplier.get();
    }


    public static List<Object> toList(String value) {
        return !ObjectUtils.isEmpty(value) ? toList(value, () -> null) : null;
    }

    public static List<Object> toList(Object value) {
        return value != null ? toList(value, () -> null) : null;
    }

    public static List<Object> toList(String value, Supplier<List<Object>> defaultSuppler) {
        if (ObjectUtils.isEmpty(value)) {
            return defaultSuppler.get();
        }
        try {

            return mapper.readValue(value, new TypeReference<LinkedList<Object>>() {});
        } catch (Exception e) {
            log.error("toList exception\n" + value, e);
        }
        return defaultSuppler.get();
    }

    public static List<Object> toList(Object value, Supplier<List<Object>> defaultSuppler) {
        if (value == null) {
            return defaultSuppler.get();
        }
        if (value instanceof List) {
            return new ArrayList<>(Collections.singleton(value));
        }
        return toList(toJSONString(value), defaultSuppler);
    }

    public static long getLong(Map<String, Object> map, String key) {
        if (ObjectUtils.isEmpty(map)) {
            return 0L;
        }
        String valueStr = String.valueOf(map.get(key));
        if (ObjectUtils.isEmpty(valueStr) || !CharSequenceUtil.isNumeric(valueStr)) {
            return 0L;
        }
        return Long.parseLong(valueStr);
    }

    public static int getInt(Map<String, Object> map, String key) {
        if (ObjectUtils.isEmpty(map)) {
            return 0;
        }
        String valueStr = String.valueOf(map.get(key));
        if (ObjectUtils.isEmpty(valueStr) || !CharSequenceUtil.isNumeric(valueStr)) {
            return 0;
        }
        return Integer.parseInt(valueStr);
    }

}


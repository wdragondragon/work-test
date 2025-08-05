package com.jdragon.websocket.msgcontrol;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JavaType;
import com.jdragon.websocket.annotation.WsParam;
import com.jdragon.websocket.model.Message;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

public class JsonArgumentConverter {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    public static Object[] parseMethodParams(Method method, JsonNode jsonNode) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            String name = "";
            if (parameter.isAnnotationPresent(WsParam.class)) {
                name = parameter.getAnnotation(WsParam.class).value();
            }
            JavaType javaType = getJavaType(parameter.getParameterizedType());
            if (StringUtils.isNotBlank(name)) {
                JsonNode paramNode = jsonNode.get(name);
                if (paramNode == null || paramNode.isNull()) {
                    args[i] = null;
                    continue;
                }
                args[i] = mapper.convertValue(paramNode, javaType);
            } else if (javaType.getRawClass() == String.class) {
                args[i] = mapper.writeValueAsString(jsonNode);
            } else if (javaType.getRawClass() == JsonNode.class) {
                args[i] = jsonNode;
            } else if (javaType.getRawClass() == Message.class) {
                args[i] = mapper.convertValue(jsonNode, javaType);
            } else {
                args[i] = mapper.convertValue(jsonNode.get("param"), javaType);
            }
        }

        return args;
    }

    /**
     * 将 JSON 转换成方法的第一个参数对象
     */
    public static Object convertParam(Method method, Map<String, Object> paramMap) throws Exception {
        Parameter parameter = method.getParameters()[0];

        JavaType javaType = getJavaType(parameter.getParameterizedType());

        // 使用 ObjectMapper 将 paramMap 转换为目标类型对象
        return mapper.convertValue(paramMap, javaType);
    }

    /**
     * 将反射参数类型解析为 Jackson 的 JavaType
     */
    public static JavaType getJavaType(java.lang.reflect.Type type) {
        return mapper.getTypeFactory().constructType(type);
    }

    // 示例方法，带泛型参数
    public void processData(DataWrapper<User> data) {
        System.out.println(data);
    }

    // 示例类
    public static class DataWrapper<T> {
        public T data;
    }

    public static class User {
        public String name;
        public int age;
    }
}
package org.example.worktest.utils.jackson;

import lombok.Getter;
import org.example.worktest.utils.jackson.ext.Base64Serializer;

@Getter
public enum CustomerJacksonSerializerEnum {
    BASE64("base64", Base64Serializer.class.getName(), "BASE64编码解码");

    /**
     * 对应key
     */
    private final String code;

    /**
     * 需要动态加载的类名
     */
    private final String className;

    /**
     * 描述
     */
    private final String desc;

    CustomerJacksonSerializerEnum(String code, String className, String desc) {
        this.code = code;
        this.className = className;
        this.desc = desc;
    }

    public static String getClassNameByCode(String bmCode) {
        for (CustomerJacksonSerializerEnum param : CustomerJacksonSerializerEnum.values()) {
            if (bmCode.equals(param.getCode())) {
                return param.getClassName();
            }
        }
        return "";
    }
}

package org.example.worktest.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReflectionUtils {
    /**
     * 根据名称生成指定的对象
     *
     * @param name 类名称
     * @return 具体的对象, 若发生异常，则返回null
     */
    public static Object objectForName(String name) {
        try {
            return Class.forName(name).newInstance();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return null;
    }
}

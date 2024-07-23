package org.example.worktest.holder;

import lombok.NonNull;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author llk
 * @date 2019-10-09 14:53
 */
public class SpringContextHolder implements ApplicationContextAware, DisposableBean {

    private static ApplicationContext applicationContext = null;

    /**
     * 实现ApplicationContextAware接口, 注入Context到静态变量中.
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {

        SpringContextHolder.applicationContext = applicationContext;
    }

    /**
     * 实现DisposableBean接口,在Context关闭时清理静态变量.
     */
    @Override
    public void destroy() {
        SpringContextHolder.clear();
    }

    /**
     * 取得存储在静态变量中的ApplicationContext.
     */
    public static ApplicationContext getApplicationContext() {
        assertContextInjected();
        return applicationContext;
    }

    /**
     * 从静态变量applicationContext中取得Bean, 自动转型为所赋值对象的类型.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        assertContextInjected();
        return (T) applicationContext.getBean(name);
    }

    /**
     * 从静态变量applicationContext中取得Bean, 自动转型为所赋值对象的类型.
     */
    public static <T> T getBean(Class<T> requiredType) {
        assertContextInjected();
        return applicationContext.getBean(requiredType);
    }

    /**
     * 清除SpringContextHolder中的ApplicationContext为Null.
     */
    public static void clear() {
        applicationContext = null;
    }


    /**
     * 获取环境变量
     */
    public static String getEnv(String key) {
        return applicationContext.getEnvironment().getProperty(key);
    }

    /**
     * 检查ApplicationContext不为空.
     */
    private static void assertContextInjected() {
        if (applicationContext == null) {
            throw new IllegalStateException("applicaitonContext未注入,请在applicationContext.xml中定义SpringContextHolder");
        }
    }

}

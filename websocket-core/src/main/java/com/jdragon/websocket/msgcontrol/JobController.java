package com.jdragon.websocket.msgcontrol;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jdragon.websocket.annotation.WsMapping;
import com.jdragon.websocket.annotation.WsRoute;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

/**
 * @Description:
 * @Author: kavaliro
 * @Date: 2018/5/27 14:09
 */

@Slf4j
@Service
public class JobController {

    private final ApplicationContext applicationContext;

    public JobController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    /**
     * 根据消息中的action字段分发到对应的controller方法
     *
     * @param message 消息内容
     */
    public void dispatchMessageByAction(String message) {
        // 这里需要解析消息内容，提取action字段
        // 然后根据action找到对应的controller方法并执行
        // 示例实现，实际应根据具体的消息格式进行解析
        try {
            // 假设message是JSON格式，包含action字段
            // 可以使用Jackson或其他JSON库解析
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(message);
            String action = jsonNode.get("action").asText();

            // 根据action找到对应的controller并执行方法
            // 这里通过ApplicationContext获取所有带有@Controller注解的bean
            String[] beanNames = applicationContext.getBeanNamesForAnnotation(WsRoute.class);

            for (String beanName : beanNames) {
                Object controller = applicationContext.getBean(beanName);
                Class<?> controllerClass = controller.getClass();

                // 获取类上的RequestMapping注解路径
                String classPath = "";
                if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping classMapping = controllerClass.getAnnotation(RequestMapping.class);
                    if (classMapping.value().length > 0) {
                        classPath = classMapping.value()[0];
                    }
                }

                // 遍历所有方法查找匹配的GetMapping
                for (Method method : controllerClass.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(WsMapping.class) && method.isAnnotationPresent(GetMapping.class)) {
                        GetMapping methodMapping = method.getAnnotation(GetMapping.class);
                        if (methodMapping.value().length > 0) {
                            String methodPath = methodMapping.value()[0];
                            // 构造完整路径
                            String fullPath = classPath + methodPath;

                            // 如果路径匹配action，则调用该方法
                            if (fullPath.equals("/" + action) || fullPath.equals(action)) {
                                Object[] objects = JsonArgumentConverter.parseMethodParams(method, jsonNode);
                                // 调用匹配的方法
                                Object result = method.invoke(controller, objects);
                                log.info("Executed method for action: {}, result: {}", action, result);
                                return;
                            }
                        }
                    }
                }
            }

            log.info("No method found for action: {}", action);
        } catch (Exception e) {
            log.error("Error processing message: {}, error: {}", message, e.getMessage(), e);
        }
    }


}
package org.example.worktest.service.impl;

import com.jdragon.message.core.MessageBroker;
import com.jdragon.websocket.core.WebSoecketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ThreadUtils;
import org.example.worktest.utils.IpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class TestWS {

    private final String key = "ws:send_token:" + IpUtils.getIp();

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MessageBroker messageBroker;

    @Autowired
    private WebSoecketSessionManager webSoecketSessionManager;

    private SendThread sendThread;

    public void join(String token) {
        Set<String> members = stringRedisTemplate.opsForSet().members(key);
        if (members == null || !members.contains(token)) {
            stringRedisTemplate.opsForSet().add(key, token);
        }
        if (sendThread == null || sendThread.isEnd()) {
            sendThread = new SendThread();
            sendThread.start();
        }
    }

    public void exit(String token) {
        stringRedisTemplate.opsForSet().remove(key, token);

        Set<String> members = stringRedisTemplate.opsForSet().members(key);
        if (CollectionUtils.isEmpty(members)) {
            sendThread.end();
            sendThread = null;
        }
    }

    public class SendThread extends Thread {

        private final AtomicBoolean sign = new AtomicBoolean(true);

        @Override
        public void run() {
            log.info("start send thread");
            while (sign.get()) {
                Set<String> members = stringRedisTemplate.opsForSet().members(key);
                Set<String> connectToken = webSoecketSessionManager.connectToken();
                if (!CollectionUtils.isEmpty(members)) {
                    for (String token : members) {
                        if (!connectToken.contains(token)) {
                            stringRedisTemplate.opsForSet().remove(key, token);
                        } else {
                            messageBroker.publish(token, token);
                        }
                    }
                } else {
                    end();
                    break;
                }
                try {
                    ThreadUtils.sleep(Duration.ofSeconds(1));
                } catch (InterruptedException e) {
                    log.error("e", e);
                }
            }
            log.info("end send thread");
        }

        public void end() {
            sign.set(false);
        }

        public boolean isEnd() {
            return !sign.get();
        }
    }


}

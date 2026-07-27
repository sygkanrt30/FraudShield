package ru.yanin.dlq_worker.service.state.redis;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Data
@Accessors(fluent = true)
@Component
@ConfigurationProperties(prefix = "spring.data.redis.key.prefix")
class KeyPrefixes {

    private String processed;

    private String lock;

    private String dead;

    private String retry;

    @PostConstruct
    public void init() {
        log.debug("Key prefix properties: processed={}, lock={}, dead={}, retry={}",
                processed, lock, dead, retry);

    }
}

package ru.yanin.dlq_worker.redis;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * @author Vyacheslav Yanin
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseRedisTest {

    @Container
    @ServiceConnection
    protected static final RedisContainer redis =
            new RedisContainer(DockerImageName.parse("redis:7.2"));

    @Autowired
    protected StringRedisTemplate redisTemplate;

    @BeforeEach
    void clearRedis() {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.serverCommands().flushAll();
            return null;
        });
    }
}

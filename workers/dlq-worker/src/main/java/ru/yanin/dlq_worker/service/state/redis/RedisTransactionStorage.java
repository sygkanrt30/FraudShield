package ru.yanin.dlq_worker.service.state.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import ru.yanin.dlq_worker.service.state.TransactionStateStorage;

import java.util.concurrent.TimeUnit;

/**
 * @author Vyacheslav Yanin
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RedisTransactionStorage implements TransactionStateStorage {

    private final StringRedisTemplate redisTemplate;
    private final KeyPrefixes keyPrefixes;

    @Override
    public boolean isAlreadyProcessed(String txId) {
        String key = keyPrefixes.processed() + txId;
        boolean exists = Boolean.TRUE.equals(redisTemplate.hasKey(key));
        if (exists) {
            log.debug("Transaction {} already processed", txId);
        }
        return exists;
    }

    @Override
    public void markAsProcessed(String txId, long ttlSeconds) {
        String key = keyPrefixes.processed() + txId;
        redisTemplate.opsForValue().set(key, "true", ttlSeconds, TimeUnit.SECONDS);
        log.debug("Marked transaction {} as processed", txId);
    }

    @Override
    public boolean isAlreadyInDeadQueue(String txId) {
        String key = keyPrefixes.dead() + txId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void markAsDead(String txId, long ttlSeconds) {
        String key = keyPrefixes.dead() + txId;
        redisTemplate.opsForValue().set(key, "true", ttlSeconds, TimeUnit.SECONDS);
        log.debug("Marked transaction {} as dead", txId);
    }

    @Override
    public boolean tryLock(String txId, long timeoutSeconds) {
        String key = keyPrefixes.lock() + txId;
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, "locked", timeoutSeconds, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(success)) {
            log.debug("Lock acquired for {}", txId);
        } else {
            log.debug("Lock already held for {}", txId);
        }

        return Boolean.TRUE.equals(success);
    }

    @Override
    public void unlock(String txId) {
        String key = keyPrefixes.lock() + txId;
        redisTemplate.delete(key);
        log.debug("Lock released for {}", txId);
    }
}

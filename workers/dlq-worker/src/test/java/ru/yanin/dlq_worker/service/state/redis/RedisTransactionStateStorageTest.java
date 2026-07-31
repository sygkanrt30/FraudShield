package ru.yanin.dlq_worker.service.state.redis;


import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yanin.dlq_worker.redis.BaseRedisTest;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Vyacheslav Yanin
 */
@Tag("integration")
class RedisTransactionStateStorageTest extends BaseRedisTest {

    @Autowired
    private KeyPrefixes keyPrefixes;

    @Autowired
    private RedisTransactionStateStorage transactionStorage;

    @Test
    void markAsProcessed_ShouldAddKeySuccessfully() {
        String txId = UUID.randomUUID().toString();
        long ttlSeconds = 10L;

        //Act
        transactionStorage.markAsProcessed(txId, ttlSeconds);

        //Assert
        assertThat(redisTemplate.hasKey(keyPrefixes.processed() + txId)).isTrue();
    }

    @Test
    void markAsProcessed_ShouldBeIdempotent() {
        String txId = UUID.randomUUID().toString();
        long ttlSeconds = 10L;

        //Act
        transactionStorage.markAsProcessed(txId, ttlSeconds);
        transactionStorage.markAsProcessed(txId, ttlSeconds);

        //Assert
        assertThat(redisTemplate.hasKey(keyPrefixes.processed() + txId)).isTrue();
    }

    @Test
    void markAsProcessed_ShouldBeReturnFalse_WhenTTLExpired() throws InterruptedException {
        String txId = UUID.randomUUID().toString();
        long ttlSeconds = 1L;

        //Act
        transactionStorage.markAsProcessed(txId, ttlSeconds);

        //Assert
        Thread.sleep(Duration.ofSeconds(2L));
        assertThat(redisTemplate.hasKey(keyPrefixes.processed() + txId)).isFalse();
    }

    @Test
    void isAlreadyProcessed_ShouldReturnTrue_WhenRedisHasKey() {
        String txId = UUID.randomUUID().toString();
        long ttlSeconds = 10L;
        transactionStorage.markAsProcessed(txId, ttlSeconds);

        //Act
        boolean result = transactionStorage.isAlreadyProcessed(txId);

        //Assert
        assertThat(result).isTrue();
    }

    @Test
    void isAlreadyProcessed_ShouldReturnFalse_WhenRedisHasNotKey() {
        String txId = UUID.randomUUID().toString();

        //Act
        boolean result = transactionStorage.isAlreadyProcessed(txId);

        //Assert
        assertThat(result).isFalse();
    }

    @Test
    void markAsDead_ShouldAddKeySuccessfully() {
        String txId = UUID.randomUUID().toString();
        long ttlSeconds = 10L;

        //Act
        transactionStorage.markAsDead(txId, ttlSeconds);

        //Assert
        assertThat(redisTemplate.hasKey(keyPrefixes.dead() + txId)).isTrue();
    }

    @Test
    void markAsDead_ShouldBeIdempotent() {
        String txId = UUID.randomUUID().toString();
        long ttlSeconds = 10L;

        //Act
        transactionStorage.markAsDead(txId, ttlSeconds);
        transactionStorage.markAsDead(txId, ttlSeconds);

        //Assert
        assertThat(redisTemplate.hasKey(keyPrefixes.dead() + txId)).isTrue();
    }

    @Test
    void markAsDead_ShouldBeReturnFalse_WhenTTLExpired() throws InterruptedException {
        String txId = UUID.randomUUID().toString();
        long ttlSeconds = 1L;

        //Act
        transactionStorage.markAsDead(txId, ttlSeconds);

        //Assert
        Thread.sleep(Duration.ofSeconds(2L));
        assertThat(redisTemplate.hasKey(keyPrefixes.dead() + txId)).isFalse();
    }

    @Test
    void isAlreadyInDeadQueue_ShouldReturnTrue_WhenRedisHasKey() {
        String txId = UUID.randomUUID().toString();
        long ttlSeconds = 10L;
        transactionStorage.markAsDead(txId, ttlSeconds);

        //Act
        boolean result = transactionStorage.isAlreadyInDeadQueue(txId);

        //Assert
        assertThat(result).isTrue();
    }

    @Test
    void isAlreadyInDeadQueue_ShouldReturnFalse_WhenRedisHasNotKey() {
        String txId = UUID.randomUUID().toString();

        //Act
        boolean result = transactionStorage.isAlreadyInDeadQueue(txId);

        //Assert
        assertThat(result).isFalse();
    }

    @Test
    void tryLock_ShouldAddLock() {
        String txId = UUID.randomUUID().toString();
        long ttlSeconds = 10L;

        //Act
        boolean result = transactionStorage.tryLock(txId, ttlSeconds);

        //Assert
        assertThat(result).isTrue();
    }

    @Test
    void tryLock_ShouldReturnFalse_WhenLockAlreadyExists() {
        String txId = UUID.randomUUID().toString();
        long ttlSeconds = 10L;
        transactionStorage.tryLock(txId, ttlSeconds);

        //Act
        boolean result = transactionStorage.tryLock(txId, ttlSeconds);

        //Assert
        assertThat(result).isFalse();
    }

    @Test
    void unlock_ShouldUnlock() {
        String txId = UUID.randomUUID().toString();
        long ttlSeconds = 10L;
        transactionStorage.tryLock(txId, ttlSeconds);

        //Act
        transactionStorage.unlock(txId);

        //Assert
        assertThat(redisTemplate.hasKey(keyPrefixes.lock() + txId)).isFalse();
    }

    @Test
    void unlock_ShouldNotThrowException_WhenLockNotExists() {
        String txId = UUID.randomUUID().toString();

        //Act & Assert
        assertThatNoException()
                .isThrownBy(() -> transactionStorage.unlock(txId));
    }
}
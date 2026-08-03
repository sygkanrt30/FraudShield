package ru.yanin.dlq_worker.service.transaction;


import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yanin.dlq_worker.repo.TransactionRepository;
import ru.yanin.shared.domain.TransactionEvent;


import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
@Tag("integration")
class TransactionServiceImplTest {

    @MockitoBean
    private TransactionRepository repository;

    @Autowired
    private TransactionServiceImpl transactionService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setUp() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("clickhouseInsert");
        circuitBreaker.reset();
    }

    @Test
    void insertTransactionSuccessfully() {
        when(repository.save(any(), anyLong())).thenReturn(Boolean.TRUE);

        //Act
        boolean result = transactionService.insertTransaction(mock(TransactionEvent.class), 123L);

        //Assert
        verify(repository).save(any(), anyLong());
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    void insertTransactionSuccessfullyAfterRetry(int attemptCount) {
        getMockSaveMethod4ResilienceCheck(attemptCount);

        //Act
        boolean result = transactionService.insertTransaction(mock(TransactionEvent.class), 123L);

        //Assert
        verify(repository, times(attemptCount)).save(any(), anyLong());
        assertThat(result).isTrue();
    }

    @Test
    void insertTransactionFailedAfterRetry() {
        int attemptCount = 6;
        getMockSaveMethod4ResilienceCheck(attemptCount);

        //Act
        boolean result = transactionService.insertTransaction(mock(TransactionEvent.class), 123L);

        //Assert
        verify(repository, times(attemptCount - 1)).save(any(), anyLong());
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {6, 7})
    void insertTransactionAttemptCountLTEMaxValue_WhenCircuitBreakerOpen(int attemptCount) {
        getMockSaveMethod4ResilienceCheck(attemptCount);

        //Act
        boolean result = transactionService.insertTransaction(mock(TransactionEvent.class), 123L);

        //Assert
        verify(repository, times(5)).save(any(), anyLong());
        assertThat(result).isFalse();
    }

    @Test
    void insertTransactionFailed_WhenTimeLimiterWorked() {
        when(repository.save(any(), anyLong())).thenAnswer(invocation -> {
            Thread.sleep(Duration.ofSeconds(7));
            return true;
        });

        //Act
        boolean result = transactionService.insertTransaction(mock(TransactionEvent.class), 123L);

        //Assert
        verify(repository).save(any(), anyLong());
        assertThat(result).isFalse();
    }

    private void getMockSaveMethod4ResilienceCheck(int attemptCount) {
        var callCount = new AtomicInteger(0);

        when(repository.save(any(), anyLong())).thenAnswer(invocation -> {
            int attempt = callCount.incrementAndGet();
            if (attempt <= (attemptCount - 1)) {
                throw new DataAccessResourceFailureException("DB error on attempt " + attempt);
            }
            return true;
        });
    }
}
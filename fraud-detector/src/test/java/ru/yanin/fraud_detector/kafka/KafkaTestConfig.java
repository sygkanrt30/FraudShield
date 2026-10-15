package ru.yanin.fraud_detector.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.yanin.shared.alert.Alert;
import ru.yanin.shared.domain.TransactionEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Vyacheslav Yanin
 */
@TestConfiguration
public class KafkaTestConfig {

    private static final String GROUP_ID_PREFIX = "fraud-detector-test-";
    private static final String ADD_TYPE_INFO_HEADERS = "spring.json.add.type.headers";

    @Bean
    public KafkaTemplate<String, Alert> alertKafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps()));
    }

    @Bean
    public KafkaTemplate<String, TransactionEvent> transactionKafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps()));
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, Alert> alertConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerProps(Alert.class));
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, TransactionEvent> transactionConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerProps(TransactionEvent.class));
    }

    @Bean(destroyMethod = "shutdownNow")
    public ExecutorService producerAlertExecutor() {
        return Executors.newFixedThreadPool(2);
    }

    @SuppressWarnings({"removal"})
    private Map<String, Object> producerProps() {
        return Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BaseKafkaTest.getBootstrapServers(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class,
                ProducerConfig.ACKS_CONFIG, "all",
                ProducerConfig.RETRIES_CONFIG, 3,
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true,
                ADD_TYPE_INFO_HEADERS, false
        );
    }

    @SuppressWarnings({"removal"})
    private Map<String, Object> consumerProps(Class<?> valueType) {
        return Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BaseKafkaTest.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID_PREFIX + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class,
                JsonDeserializer.TRUSTED_PACKAGES, "*",
                JsonDeserializer.VALUE_DEFAULT_TYPE, valueType.getName()
        );
    }
}

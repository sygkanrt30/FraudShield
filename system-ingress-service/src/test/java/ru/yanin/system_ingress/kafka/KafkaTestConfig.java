package ru.yanin.system_ingress.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import ru.yanin.shared.domain.TransactionEvent;

import java.util.Map;
import java.util.UUID;


/**
 * @author Vyacheslav Yanin
 */
@TestConfiguration
public class KafkaTestConfig {

    @Bean
    @Primary
    public KafkaTemplate<String, TransactionEvent> testKafkaTemplate() {
        Map<String, Object> producerProps = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BaseKafkaTest.getBootstrapServers(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class,
                ProducerConfig.ACKS_CONFIG, "all",
                ProducerConfig.RETRIES_CONFIG, 3,
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true
        );

        DefaultKafkaProducerFactory<String, TransactionEvent> factory =
                new DefaultKafkaProducerFactory<>(producerProps);
        return new KafkaTemplate<>(factory);
    }

    @Bean
    @Primary
    public DefaultKafkaConsumerFactory<String, TransactionEvent> testConsumerFactory() {
        Map<String, Object> consumerProps = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BaseKafkaTest.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class,
                JacksonJsonDeserializer.TRUSTED_PACKAGES, "*"
        );

        return new DefaultKafkaConsumerFactory<>(consumerProps);
    }
}

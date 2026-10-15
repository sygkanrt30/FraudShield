package ru.yanin.fraud_detector.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Vyacheslav Yanin
 */
public abstract class BaseKafkaTest {

    protected static final String ALERT_TOPIC = "fraud-alerts";
    protected static final String MANUAL_REVIEW_TOPIC = "manual-review";

    private static final Duration RECORD_AWAIT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(500);

    @Container
    protected static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(
            DockerImageName.parse("apache/kafka:4.1.0")
    );

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
        registry.add("app.kafka.topics.alert", () -> ALERT_TOPIC);
        registry.add("app.kafka.topics.manual-review", () -> MANUAL_REVIEW_TOPIC);
    }

    public static KafkaContainer getKafkaContainer() {
        return KAFKA_CONTAINER;
    }

    public static String getBootstrapServers() {
        return KAFKA_CONTAINER.getBootstrapServers();
    }

    protected <T> KafkaConsumer<String, T> consumer(DefaultKafkaConsumerFactory<String, T> factory, String topic) {
        KafkaConsumer<String, T> consumer = (KafkaConsumer<String, T>) factory.createConsumer();
        consumer.subscribe(List.of(topic));
        consumer.poll(Duration.ofMillis(200));
        return consumer;
    }

    protected <T> ConsumerRecord<String, T> awaitRecord(KafkaConsumer<String, T> consumer, Predicate<T> predicate) {
        long deadline = System.nanoTime() + RECORD_AWAIT_TIMEOUT.toNanos();
        while (System.nanoTime() < deadline) {
            ConsumerRecords<String, T> records = consumer.poll(POLL_INTERVAL);
            for (ConsumerRecord<String, T> record : records) {
                if (predicate.test(record.value())) {
                    return record;
                }
            }
        }
        throw new AssertionError("No matching record received within " + RECORD_AWAIT_TIMEOUT);
    }
}

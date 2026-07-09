package ru.yanin.system_ingress.kafka;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * @author Vyacheslav Yanin
 */
public abstract class BaseKafkaTest {

    protected static final String TOPIC = "raw-transactions";

    @Container
    protected static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(
            DockerImageName.parse("apache/kafka:4.1.0")
    );

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
        registry.add("app.kafka.topics.raw-transactions", () -> TOPIC);
    }

    public static KafkaContainer getKafkaContainer() {
        return KAFKA_CONTAINER;
    }

    public static String getBootstrapServers() {
        return KAFKA_CONTAINER.getBootstrapServers();
    }
}

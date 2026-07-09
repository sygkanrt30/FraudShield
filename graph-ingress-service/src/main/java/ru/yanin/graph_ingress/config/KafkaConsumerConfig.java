package ru.yanin.graph_ingress.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import ru.yanin.shared.domain.TransactionEvent;

/**
 * @author Vyacheslav Yanin
 */
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransactionEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, TransactionEvent> consumerFactory,
            @Value("${app.kafka.thread.count}") int threadCount) {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, TransactionEvent>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(threadCount);
        factory.setBatchListener(false);
        return factory;
    }
}

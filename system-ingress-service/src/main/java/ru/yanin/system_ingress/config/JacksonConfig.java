package ru.yanin.system_ingress.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Vyacheslav Yanin
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper kafkaObjectMapper() {
        ObjectMapper kafkaObjectMapper = new ObjectMapper();
        kafkaObjectMapper.registerModule(new JavaTimeModule());
        kafkaObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return kafkaObjectMapper;
    }
}

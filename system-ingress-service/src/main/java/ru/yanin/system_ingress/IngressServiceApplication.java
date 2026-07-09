package ru.yanin.system_ingress;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class IngressServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IngressServiceApplication.class, args);
    }

}

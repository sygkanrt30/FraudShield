package ru.yanin.dlq_worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DlqWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DlqWorkerApplication.class, args);
    }

}

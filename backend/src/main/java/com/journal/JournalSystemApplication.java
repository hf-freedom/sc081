package com.journal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JournalSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(JournalSystemApplication.class, args);
    }
}

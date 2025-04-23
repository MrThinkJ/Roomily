package com.c2se.roomily;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RoomilyApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoomilyApplication.class, args);
    }
}

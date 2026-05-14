package com.nexra.hrms.nexra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Bootstraps the Nexra modular monolith container.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class NexraApplication {

    public static void main(final String[] args) {
        SpringApplication.run(NexraApplication.class, args);
    }
}

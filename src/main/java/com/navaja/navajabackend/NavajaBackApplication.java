package com.navaja.navajabackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.cache.annotation.EnableCaching;

@EnableAsync
@EnableCaching
@EnableScheduling
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.navaja.navajabackend.repositories")
public class NavajaBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(NavajaBackApplication.class, args);
    }

}



package com.ceedpods.crmbuild;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CrmBuildApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrmBuildApplication.class, args);
    }
}
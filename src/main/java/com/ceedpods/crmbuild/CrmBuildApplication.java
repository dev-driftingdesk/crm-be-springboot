package com.ceedpods.crmbuild;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CrmBuildApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrmBuildApplication.class, args);
    }
}
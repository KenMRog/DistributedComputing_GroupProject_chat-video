package com.screenshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("com.screenshare.entity")
public class ScreenshareBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScreenshareBackendApplication.class, args);
    }
}

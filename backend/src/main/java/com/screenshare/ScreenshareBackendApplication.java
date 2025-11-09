package com.screenshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan("com.screenshare.entity")
@ComponentScan("com.screenshare")
public class ScreenshareBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScreenshareBackendApplication.class, args);
    }
}

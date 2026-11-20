package com.mrtlptkn.springbatchpartitiondemoapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringBatchPartitionDemoAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchPartitionDemoAppApplication.class, args);
    }

}

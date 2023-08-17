package com.dfc.exchange_api.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    /**
     * Creating a RestTemplate bean for external API HTTP Connection
     */
    @Bean
    RestTemplate restTemplate(){
        return new RestTemplate();
    }

}

package com.dissertation.backend.common;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class DataSeederConfig {

    @Bean
    CommandLineRunner seedData(DataSeeder dataSeeder) {
        return args -> dataSeeder.seed();
    }
}
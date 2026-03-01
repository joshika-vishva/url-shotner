package com.sniplink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SnipLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnipLinkApplication.class, args);
        System.out.println("\n==============================================");
        System.out.println("🚀 SnipLink Application Started Successfully!");
        System.out.println("📍 Access at: http://localhost:8080");
        System.out.println("==============================================\n");
    }
}

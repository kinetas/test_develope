package com.trpg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * webGame-contextTRPG — Main Application Entry Point
 *
 * <p>Spring Boot application class that bootstraps the TRPG web game server.
 * Scans all sub-packages under com.trpg automatically.</p>
 *
 * Architecture: MVC (Controller / Service / Repository)
 * Security   : Spring Security + JWT
 * Realtime   : WebSocket (STOMP)
 * View       : Thymeleaf
 * Database   : H2 (dev) — replace with MySQL/PostgreSQL for production
 */
@SpringBootApplication
public class TrpgApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrpgApplication.class, args);
    }
}

package com.example.bookstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;

@Configuration
public class DatabaseConfig {

    @Bean
    public JdbcDialect jdbcDialect() {
        return new SqliteDialect();
    }
}


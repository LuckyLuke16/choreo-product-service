package com.example.productservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class DatabaseInitializer {

    @Bean
    public JdbcTemplate jdbcTemplate() {return new JdbcTemplate();}

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void fillDatabaseWithTestData() { this.jdbcTemplate.execute("RUNSCRIPT FROM 'classpath:database/test-data.sql'");}

    public static String toJson(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

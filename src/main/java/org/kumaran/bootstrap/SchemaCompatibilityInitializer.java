package org.kumaran.bootstrap;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class SchemaCompatibilityInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    public SchemaCompatibilityInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        Boolean usersTableExists = jdbcTemplate.queryForObject(
                "select to_regclass('public.users') is not null",
                Boolean.class
        );

        if (!Boolean.TRUE.equals(usersTableExists)) {
            return;
        }

        jdbcTemplate.execute("alter table users add column if not exists password_reset_requested boolean not null default false");
        jdbcTemplate.execute("alter table users add column if not exists password_reset_requested_at varchar(255)");
        jdbcTemplate.execute("alter table users add column if not exists force_password_change boolean not null default false");
        jdbcTemplate.execute("alter table users add column if not exists temporary_password_issued_at varchar(255)");
    }
}


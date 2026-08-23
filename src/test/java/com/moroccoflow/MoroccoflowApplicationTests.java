package com.moroccoflow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MoroccoflowApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayMigrationCreatedDomainTables() {
        Set<String> tables = jdbcTemplate.queryForList(
                        "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'",
                        String.class)
                .stream().collect(Collectors.toSet());

        assertThat(tables).contains("cities", "roads", "intersections");
    }
}
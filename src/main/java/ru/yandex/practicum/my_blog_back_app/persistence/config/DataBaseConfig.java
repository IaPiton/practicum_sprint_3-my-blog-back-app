package ru.yandex.practicum.my_blog_back_app.persistence.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@ComponentScan("ru.yandex.practicum.my_blog_back_app.persistence")
public class DataBaseConfig {

    @Bean
    public DataSource dataSource() {
        Dotenv dotenv = Dotenv.configure()
                .directory("./")
                .filename(".env")
                .ignoreIfMissing()
                .load();

        HikariConfig config = new HikariConfig();

        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s",
                dotenv.get("POSTGRES_HOST"),
                dotenv.get("POSTGRES_PORT"),
                dotenv.get("POSTGRES_DB")
        );

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(dotenv.get("POSTGRES_USER"));
        config.setPassword(dotenv.get("POSTGRES_PASSWORD"));
        return new HikariDataSource(config);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
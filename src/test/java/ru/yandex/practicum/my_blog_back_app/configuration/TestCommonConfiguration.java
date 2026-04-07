package ru.yandex.practicum.my_blog_back_app.configuration;

import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.my_blog_back_app.configuration.testconteiner.PgTestContainerConfiguration;


@SpringJUnitConfig({TestConfiguration.class, PgTestContainerConfiguration.class})
public class TestCommonConfiguration {
}

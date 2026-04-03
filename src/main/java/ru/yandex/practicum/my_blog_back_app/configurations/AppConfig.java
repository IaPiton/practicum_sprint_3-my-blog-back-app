package ru.yandex.practicum.my_blog_back_app.configurations;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@ComponentScan("ru.yandex.practicum.my_blog_back_app")
public class AppConfig implements WebMvcConfigurer {

}

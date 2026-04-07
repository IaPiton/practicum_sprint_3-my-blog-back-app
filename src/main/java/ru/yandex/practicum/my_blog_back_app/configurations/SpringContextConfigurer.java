package ru.yandex.practicum.my_blog_back_app.configurations;

import jakarta.servlet.ServletContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import ru.yandex.practicum.my_blog_back_app.persistence.config.DataBaseConfig;

@Component
public class SpringContextConfigurer {

    public AnnotationConfigWebApplicationContext createAndConfigureContext(ServletContext servletContext) {
        AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
        appContext.register(WebConfig.class);
        appContext.register(DataBaseConfig.class);
        appContext.setServletContext(servletContext);
        appContext.refresh();

        return appContext;
    }

    public DispatcherServlet createDispatcherServlet(AnnotationConfigWebApplicationContext appContext) {
        return new DispatcherServlet(appContext);
    }
}

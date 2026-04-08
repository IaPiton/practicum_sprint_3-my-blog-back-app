package ru.yandex.practicum.my_blog_back_app;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import ru.yandex.practicum.my_blog_back_app.configurations.MultipartConfigurer;
import ru.yandex.practicum.my_blog_back_app.configurations.SpringContextConfigurer;
import ru.yandex.practicum.my_blog_back_app.configurations.TomcatConfig;

import java.io.File;

public class Application {
    private static final int PORT = 8080;
    private static final String CONTEXT_PATH = "";
    private static final String DISPATCHER_SERVLET_NAME = "dispatcherServlet";

    public static void main(String[] args) throws LifecycleException {
        TomcatConfig tomcatConfig = new TomcatConfig();
        MultipartConfigurer multipartConfigurer = new MultipartConfigurer();
        SpringContextConfigurer springConfigurer = new SpringContextConfigurer();

        Tomcat tomcat = tomcatConfig.configureTomcat(PORT);
        Context context = tomcatConfig.configureContext(tomcat, CONTEXT_PATH);

        File uploadDir = multipartConfigurer.createUploadDirectory();
        multipartConfigurer.configureMultipartForContext(context, uploadDir);

        AnnotationConfigWebApplicationContext appContext =
                springConfigurer.createAndConfigureContext(context.getServletContext());
        DispatcherServlet dispatcherServlet =
                springConfigurer.createDispatcherServlet(appContext);

        Tomcat.addServlet(context, DISPATCHER_SERVLET_NAME, dispatcherServlet);
        context.addServletMappingDecoded("/*", DISPATCHER_SERVLET_NAME);

        multipartConfigurer.configureMultipartForServlet(context, DISPATCHER_SERVLET_NAME, uploadDir);

        tomcat.start();
        tomcat.getServer().await();
    }
}
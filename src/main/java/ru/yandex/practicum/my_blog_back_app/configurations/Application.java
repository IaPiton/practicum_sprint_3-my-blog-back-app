package ru.yandex.practicum.my_blog_back_app.configurations;


import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.File;

public class Application {
    public static void main(String[] args) throws Exception {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();


        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);

        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        tomcat.setBaseDir(baseDir.getAbsolutePath());

        Context tomcatContext = tomcat.addWebapp("", baseDir.getAbsolutePath());

        context.setServletContext(tomcatContext.getServletContext());
        context.register(AppConfig.class);
        context.refresh();

        DispatcherServlet dispatcherServlet = new DispatcherServlet(context);

        Tomcat.addServlet(tomcatContext, "dispatcherServlet", dispatcherServlet).setLoadOnStartup(1);
        tomcatContext.addServletMappingDecoded("/*", "dispatcherServlet");

        // 6. Запускаем Tomcat
        tomcat.start();
        System.out.println("Приложение запущено на http://localhost:8080");
        tomcat.getServer().await();
    }
}

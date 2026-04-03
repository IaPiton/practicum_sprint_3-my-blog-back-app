package ru.yandex.practicum.my_blog_back_app;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import ru.yandex.practicum.my_blog_back_app.configurations.AppConfig;
import ru.yandex.practicum.my_blog_back_app.persistence.config.DataBaseConfig;

import java.io.File;

public class Application {

    public static void main(String[] args) throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector(); // Инициализируем коннектор

        String baseDir = System.getProperty("java.io.tmpdir");
        tomcat.setBaseDir(baseDir);

        Context context = tomcat.addContext("", null);

        WebResourceRoot resources = new StandardRoot(context);
        File classDir = new File("target/classes");
        resources.addPreResources(new DirResourceSet(
                resources,
                "/WEB-INF/classes",
                classDir.getAbsolutePath(),
                "/"
        ));
        context.setResources(resources);

        AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
        appContext.register(AppConfig.class);
        appContext.register(DataBaseConfig.class);
        appContext.setServletContext(context.getServletContext());
        appContext.refresh();

        DispatcherServlet dispatcherServlet = new DispatcherServlet(appContext);
        Tomcat.addServlet(context, "dispatcherServlet", dispatcherServlet);
        context.addServletMappingDecoded("/*", "dispatcherServlet");

        tomcat.start();
        tomcat.getServer().await();
    }
}
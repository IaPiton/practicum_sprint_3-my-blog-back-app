package ru.yandex.practicum.my_blog_back_app.configurations;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class TomcatConfig {

    private static final String BASE_DIR_PROPERTY = "java.io.tmpdir";
    private static final String CLASSES_DIR = "target/classes";
    private static final String WEB_INF_CLASSES = "/WEB-INF/classes";

    private final Tomcat tomcat;

    public TomcatConfig() {
        this.tomcat = new Tomcat();
    }

    public Tomcat configureTomcat(int port) {
        tomcat.setPort(port);
        tomcat.getConnector();

        String baseDir = System.getProperty(BASE_DIR_PROPERTY);
        tomcat.setBaseDir(baseDir);

        return tomcat;
    }

    public Context configureContext(Tomcat tomcat, String contextPath) {
        Context context = tomcat.addContext(contextPath, null);
        configureResources(context);
        return context;
    }

    private void configureResources(Context context) {
        WebResourceRoot resources = new StandardRoot(context);
        File classDir = new File(CLASSES_DIR);
        resources.addPreResources(new DirResourceSet(
                resources,
                WEB_INF_CLASSES,
                classDir.getAbsolutePath(),
                "/"
        ));
        context.setResources(resources);
    }
}

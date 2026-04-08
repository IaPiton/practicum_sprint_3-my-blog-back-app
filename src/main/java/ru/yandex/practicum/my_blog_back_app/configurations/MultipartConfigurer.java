package ru.yandex.practicum.my_blog_back_app.configurations;

import jakarta.servlet.MultipartConfigElement;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class MultipartConfigurer {

    private static final int MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_REQUEST_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int FILE_SIZE_THRESHOLD = 0;
    private static final String UPLOADS_DIR = "/uploads";

    public File createUploadDirectory() {
        String tempDir = System.getProperty("java.io.tmpdir") + UPLOADS_DIR;
        File uploadDir = new File(tempDir);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        return uploadDir;
    }

    public void configureMultipartForContext(Context context, File uploadDir) {
        context.setAllowCasualMultipartParsing(true);
        context.getServletContext().setAttribute("jakarta.servlet.context.tempdir", uploadDir);
    }

    public void configureMultipartForServlet(Context context, String servletName, File uploadDir) {
        MultipartConfigElement multipartConfig = new MultipartConfigElement(
                uploadDir.getAbsolutePath(),
                MAX_FILE_SIZE,
                MAX_REQUEST_SIZE,
                FILE_SIZE_THRESHOLD
        );

        Wrapper wrapper = (Wrapper) context.findChild(servletName);
        if (wrapper != null) {
            wrapper.setMultipartConfigElement(multipartConfig);
        }
    }
}
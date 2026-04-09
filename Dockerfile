FROM bellsoft/liberica-runtime-container:jdk-21-musl


COPY target/my-blog-back-app-1.0.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
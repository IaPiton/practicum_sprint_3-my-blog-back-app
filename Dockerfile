FROM bellsoft/liberica-runtime-container:jdk-21-musl


COPY build/libs/my-blog-back-app.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
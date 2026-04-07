FROM bellsoft/liberica-runtime-container:jdk-21-musl

WORKDIR /app

RUN mkdir -p /app/target/classes
RUN mkdir -p /app/src/main/webapp

COPY target/classes /app/target/classes/

COPY target/my-blog-back-app.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
# Dùng JDK 17 chính thức từ Eclipse Temurin
FROM eclipse-temurin:17-jdk-jammy

LABEL maintainer="huuchuongdev"

# Thư mục làm việc trong container
WORKDIR /app

# Copy file jar đã build từ target vào container
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Container lắng nghe port 8080
EXPOSE 8080

# Lệnh chạy app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

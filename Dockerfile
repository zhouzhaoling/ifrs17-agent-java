# 使用轻量级 JDK 21 基础镜像
FROM eclipse-temurin:21-jre-alpine

# 设置工作目录
WORKDIR /app

# 将 Maven 打好的 jar 包复制进去（假定我们在本地已经打包）
COPY target/*.jar app.jar

# 暴露应用端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
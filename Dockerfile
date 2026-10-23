# 多阶段构建：前端（Vite 构建）→ 后端（Maven 打包并内嵌前端产物）→ 运行时
FROM node:20-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install --no-audit --no-fund
COPY frontend/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-17 AS backend
WORKDIR /app
COPY backend/pom.xml backend/pom.xml
RUN mvn -q -f backend/pom.xml -DskipTests dependency:go-offline
COPY backend/src backend/src
COPY --from=frontend /app/frontend/dist backend/src/main/resources/static
RUN mvn -q -f backend/pom.xml -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=backend /app/backend/target/qingjian-backend-0.2.0.jar app.jar
ENV SPRING_PROFILES_ACTIVE=cloud
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY backend-v2/pom.xml backend-v2/pom.xml
RUN mvn -f backend-v2/pom.xml dependency:go-offline -B

COPY backend-v2/src backend-v2/src
RUN mvn -f backend-v2/pom.xml -DskipTests package -B

FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache curl \
    && addgroup -S appgroup \
    && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=build /workspace/backend-v2/target/backend-v2-*.jar /app/app.jar

USER appuser

ENV JAVA_OPTS=""

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=45s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

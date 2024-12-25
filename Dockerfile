FROM eclipse-temurin:21 AS builder
WORKDIR /extracted
COPY app/build/libs/app.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher

FROM eclipse-temurin:21 AS production
RUN addgroup --system appgroup && adduser --system appuser --ingroup appgroup
WORKDIR /application

COPY --from=builder /extracted/app/dependencies/ ./
COPY --from=builder /extracted/app/spring-boot-loader/ ./
COPY --from=builder /extracted/app/snapshot-dependencies/ ./
COPY --from=builder /extracted/app/application/ ./

USER appuser

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
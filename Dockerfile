# 1단계: 빌드
FROM gradle:8.5-jdk17 AS build
COPY --chown=gradle:gradle . /app
WORKDIR /app
RUN gradle build --no-daemon

# 2단계: 실행
FROM openjdk:17
WORKDIR /app

# 실행할 JAR만 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 실행 시 환경 변수로 주입받은 값 활용
# 실제 app.jar 내부에서 환경 변수 참고해야 함
ENTRYPOINT ["java", "-jar", "app.jar"]

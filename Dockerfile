# ---------------------------
# 1. 빌드 스테이지
# ---------------------------
FROM gradle:jdk17-alpine AS build

WORKDIR /app

# 모든 소스 코드 복사
COPY . .

# 🛠 설정 파일도 복사 (Jenkins에서 미리 위치에 넣어주는 것을 전제로 함)
COPY src/main/resources/secret.yml src/main/resources/secret.yml
COPY src/main/resources/keystore.p12 src/main/resources/keystore.p12

# Gradle 빌드 (테스트 포함)
RUN gradle clean build --no-daemon

# ---------------------------
# 2. 실행 스테이지
# ---------------------------
FROM openjdk:17-alpine

WORKDIR /app

# 빌드 결과 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar ./

# plain 제외 JAR을 app.jar로 변경
RUN mv $(find . -maxdepth 1 -name "*.jar" ! -name "*plain*.jar") app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

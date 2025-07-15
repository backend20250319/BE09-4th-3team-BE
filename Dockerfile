# OpenJDK 17이 설치된 Alpine Linux 이미지를 기반으로 한다.
#FROM openjdk:17-alpine

# 빌드된 JAR 파일을 컨테이너의 루트 디렉토리에 app.jar로 복사한다.
#COPY build/libs/*.jar app.jar

# 컨테이너 시작 시 app.jar 파일을 실행하는 명령을 설정한다.
#ENTRYPOINT ["java", "-jar", "app.jar"]

# ---------------------------------------------------------------------
## 1. 빌드 스테이지 시작
# Gradle 이미지를 가져오고, 빌드 스테이지를 'build'라는 이름으로 설정한다.
FROM gradle:jdk17-alpine AS build

# 컨테이너 내부의 작업 디렉토리를 /app으로 설정한다.
WORKDIR /app

# 현재 디렉토리 내의 모든 파일과 폴더를 컨테이너의 /app 디렉토리로 복사한다.
COPY . .

# Gradle을 사용하여 프로젝트를 빌드한다. (daemon 프로세스는 사용하지 않는다.)
RUN gradle clean build --no-daemon

## 2. 실행 스테이지 시작
# OpenJDK 17 버전의 이미지를 가져와 JVM 환경을 구축한다.
FROM openjdk:17-alpine

# 빌드 스테이지에서 생성된 JAR 파일을 복사한다.
COPY --from=build /app/build/libs/*.jar ./

# JAR 파일을 나열하고 grep을 사용해 'plain'이라는 단어가 포함되지 않은 줄을 선택하여 app.jar로 변경한다.
RUN mv $(ls *.jar | grep -v plain) app.jar

# app.jar를 리눅스 환경에서 실행하여 스프링 부트 서버를 시작한다.
ENTRYPOINT ["java", "-jar", "app.jar"]

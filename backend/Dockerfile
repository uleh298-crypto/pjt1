FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Gradle Wrapper 설정
COPY gradlew .
COPY gradle gradle
RUN chmod +x ./gradlew

# 의존성 설정 파일 복사
COPY build.gradle.kts settings.gradle.kts ./

# 의존성 다운로드 (캐시 레이어)
RUN --mount=type=cache,target=/root/.gradle/caches --mount=type=cache,target=/root/.gradle/wrapper ./gradlew dependencies --no-daemon --parallel

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드 (테스트 제외)
RUN --mount=type=cache,target=/root/.gradle/caches --mount=type=cache,target=/root/.gradle/wrapper ./gradlew bootJar --no-daemon -x test --parallel --info

# 런타임 스테이지
FROM eclipse-temurin:21-jre
WORKDIR /app

# 비root 사용자 생성 (보안)
RUN addgroup --system spring && adduser --system spring --ingroup spring

# JAR 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# Entrypoint 스크립트 복사 및 권한 설정
COPY entrypoint.sh .
RUN chmod +x entrypoint.sh

EXPOSE 8080

# JVM 최적화 옵션 포함
ENTRYPOINT ["./entrypoint.sh", "java", \
    "-Xms512m", \
    "-Xmx1024m", \
    "-XX:+UseG1GC", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
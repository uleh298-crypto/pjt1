plugins {
	java
	id("org.springframework.boot") version "4.0.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.ssafy"
version = "0.0.1-SNAPSHOT"
description = "ssabre app backend server"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	
    implementation("tools.jackson.core:jackson-databind:3.0.3")
    implementation("tools.jackson.core:jackson-core:3.0.3")

	
	runtimeOnly("org.postgresql:postgresql")
	
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")
    implementation("com.google.firebase:firebase-admin:9.2.0")
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
	
	// Monitoring: Actuator + Prometheus
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("io.micrometer:micrometer-registry-prometheus")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// apps/payment-service/build.gradle.kts


plugins {
  id("org.springframework.boot")
  id("io.spring.dependency-management")
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-amqp")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

  runtimeOnly("org.postgresql:postgresql")
  runtimeOnly("io.micrometer:micrometer-registry-prometheus")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.bootJar { archiveFileName.set("payment-service.jar") }

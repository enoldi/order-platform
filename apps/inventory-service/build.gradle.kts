// apps/inventory-service/build.gradle.kts

plugins {
  id("org.springframework.boot")
  id("io.spring.dependency-management")
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-amqp")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  runtimeOnly("org.postgresql:postgresql")

  runtimeOnly("io.micrometer:micrometer-registry-prometheus")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.testcontainers:junit-jupiter:1.20.2")
  testImplementation("org.testcontainers:postgresql:1.20.2")
}

tasks.bootJar { archiveFileName.set("inventory-service.jar") }

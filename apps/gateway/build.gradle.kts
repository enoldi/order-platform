plugins {
  id("org.springframework.boot")
  id("io.spring.dependency-management")
}

extra["springCloudVersion"] = "2023.0.6"

configurations.all {
  resolutionStrategy.failOnVersionConflict()
}

dependencyManagement {
  imports {
    mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
  }
}

dependencies {
  implementation("org.springframework.cloud:spring-cloud-starter-gateway")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  runtimeOnly("io.micrometer:micrometer-registry-prometheus")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.bootJar { archiveFileName.set("gateway.jar") }

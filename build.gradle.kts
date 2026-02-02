plugins {
  id("org.springframework.boot") apply false
  id("io.spring.dependency-management") apply false
  java
}

allprojects {
  repositories { mavenCentral() }
}

subprojects {
  apply(plugin = "java")

  group = "com.chaars"
  version = "0.0.1"

  java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
  }

  tasks.withType<Test> { useJUnitPlatform() }
}

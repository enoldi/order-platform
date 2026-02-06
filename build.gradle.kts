import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
  id("org.springframework.boot") apply false
  id("io.spring.dependency-management") apply false
  // Vulnerability scanning
  id("org.owasp.dependencycheck") version "12.1.0"
  java
}

allprojects {
  repositories { mavenCentral() }
  extra["spring-framework.version"] = "6.2.11"
}

dependencyCheck {
  // Fails the build if a vulnerability meets/exceeds this CVSS score
  failBuildOnCVSS = 11f

  // Scans common dependency configs (works well for Spring Boot projects)
  scanConfigurations = listOf(
    "compileClasspath",
    "runtimeClasspath",
    "testCompileClasspath",
    "testRuntimeClasspath"
  )

  suppressionFile = "dependency-check-suppressions.xml"

  // No ReportGenerator import: use strings
  formats = listOf("HTML", "JSON")

  // Optional: reduce noise from test-only deps
  skipTestGroups = true
}

subprojects {
  apply(plugin = "java")
  apply(plugin = "io.spring.dependency-management")

  configure<DependencyManagementExtension> {
    dependencies {
      dependency("io.netty:netty-codec-http2:4.1.124.Final")
      dependency("io.netty:netty-handler:4.1.118.Final")
    }
  }

  group = "com.chaars"
  version = "0.0.1"

  java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
  }

  tasks.withType<Test> { useJUnitPlatform() }
}
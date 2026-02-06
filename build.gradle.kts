plugins {
  id("org.springframework.boot") apply false
  id("io.spring.dependency-management") apply false
  // Vulnerability scanning
  id("org.owasp.dependencycheck") version "12.1.0"
  java
}

allprojects {
  repositories { mavenCentral() }
  configurations.configureEach {
    resolutionStrategy {
//      force("org.apache.commons:commons-lang3:3.18.0")
    }
  }
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

  suppressionFire = "dependency-check-suppressions.xml"

  // No ReportGenerator import: use strings
  formats = listOf("HTML", "JSON")

  // Optional: reduce noise from test-only deps
  skipTestGroups = true
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
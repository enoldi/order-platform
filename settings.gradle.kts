pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
  plugins {
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.6"
  }
}

rootProject.name = "order-platform"

include(
  "apps:gateway",
  "apps:order-service",
  "apps:payment-service",
  "apps:inventory-service",
  "apps:notification-service",
)

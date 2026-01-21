plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.spring)
    alias(libs.plugins.springboot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.jpa)
    alias(libs.plugins.noarg)
}


group = "org.pv293"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.jackson)
    implementation(libs.kotlin.reflect)
    runtimeOnly(libs.micrometer.prometheus)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.junit)
    testRuntimeOnly(libs.junit.launcher)
    implementation(libs.axon)
    implementation(libs.axon.kotlin)
    implementation(libs.axon.spring)
    implementation(libs.axon.spring.boot.starter)
    implementation(libs.kotlinx.datetime)
    implementation(libs.springboot.starter.data.jpa)
    implementation(libs.h2.database)
    implementation(libs.postgres)
    implementation(libs.logback.classic)
    implementation(libs.logback.loki.appender)
    implementation(libs.logstash.logback.encoder)
    implementation(libs.springdoc.openapi.ui)
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    mainClass.set("org.pv293.kotlinseminar.coursesService.CoursesServiceApplicationKt")
}
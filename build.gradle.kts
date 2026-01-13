import org.gradle.kotlin.dsl.libs

plugins {
    alias(libs.plugins.jvm) apply false
    alias(libs.plugins.spring) apply false
    alias(libs.plugins.springboot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.jpa) apply false
    alias(libs.plugins.noarg) apply false
}


allprojects {
    group = "org.pv293"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.ktlint)
}

group = "no.novari"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    withSourcesJar()
}

kotlin {
    compilerOptions {
        // Spring Boot 4.1 manages Kotlin 2.3, so apps may run with a 2.3 stdlib.
        apiVersion = KotlinVersion.KOTLIN_2_3
    }
}

ktlint {
    version = libs.versions.ktlint.cli
}

dependencyLocking {
    lockAllConfigurations()
}

dependencies {
    implementation(platform(libs.spring.boot.dependencies))
    testImplementation(platform(libs.spring.boot.dependencies))

    implementation("com.github.ben-manes.caffeine:caffeine")

    compileOnly(platform(libs.spring.boot.dependencies))
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("io.micrometer:micrometer-core")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-micrometer-metrics")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    repositories {
        maven {
            url = uri("https://repo.fintlabs.no/releases")
            credentials {
                username = System.getenv("REPOSILITE_USERNAME")
                password = System.getenv("REPOSILITE_PASSWORD")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            // Publish the versions resolved from the Spring Boot BOM, so consumers do not need the BOM.
            versionMapping {
                allVariants {
                    fromResolutionResult()
                }
            }
        }
    }
}

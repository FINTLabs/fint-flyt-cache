plugins {
    `java-library`
    `maven-publish`
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

dependencyLocking {
    lockAllConfigurations()
}

dependencies {
    implementation(platform(libs.spring.boot.dependencies))
    compileOnly(platform(libs.spring.boot.dependencies))
    annotationProcessor(platform(libs.spring.boot.dependencies))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation(libs.ehcache)
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
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

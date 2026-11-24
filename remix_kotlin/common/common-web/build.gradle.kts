plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.lombok")
}

dependencies {
    api(project(":common:common-core"))
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.cloud:spring-cloud-starter-openfeign")
    testImplementation(kotlin("test"))
}

repositories {
    mavenCentral()
}

plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.lombok")
}

dependencies {
    api(project(":common:common-core"))
    api("org.springframework.cloud:spring-cloud-starter-openfeign")
}
repositories {
    mavenCentral()
}
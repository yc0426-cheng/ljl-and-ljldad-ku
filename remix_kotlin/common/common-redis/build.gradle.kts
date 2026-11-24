plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.lombok")
    kotlin("kapt")
}

dependencies {
    api(project(":common:common-core"))
    api("org.springframework.boot:spring-boot-starter-data-redis")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
repositories {
    mavenCentral()
}
kapt {
    keepJavacAnnotationProcessors = true
}
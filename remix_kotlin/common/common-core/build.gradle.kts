plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.lombok")
    kotlin("kapt")
}

dependencies {
    api("com.baomidou:mybatis-plus-spring-boot3-starter")
    api("com.baomidou:mybatis-plus-jsqlparser")
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-json")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.mapstruct:mapstruct:1.6.3")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
repositories {
    mavenCentral()
}
kapt {
    keepJavacAnnotationProcessors = true
}
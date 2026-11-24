plugins {
    id("org.springframework.boot") version "3.2.12"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.spring") version "2.1.20"
    kotlin("kapt")
    kotlin("plugin.lombok")
}

group = "org.yc"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common:common-core"))
    implementation(project(":common:common-web"))
    implementation(project(":common:common-redis"))
    implementation(project(":api:api-system"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.baomidou:mybatis-plus-spring-boot3-starter")
    implementation("com.baomidou:mybatis-plus-jsqlparser")
    implementation("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery")
    implementation("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-config")
    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")
    implementation("org.apache.fesod:fesod-sheet")
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("org.flywaydb:flyway-core")
    runtimeOnly("org.flywaydb:flyway-mysql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}
kapt {
    keepJavacAnnotationProcessors = true
}
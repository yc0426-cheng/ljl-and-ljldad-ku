plugins {
    id("org.springframework.boot") version "3.2.12" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
    kotlin("jvm") version "2.1.20" apply false
    kotlin("plugin.spring") version "2.1.20" apply false
}

allprojects {
    group = "com.zz.learn"
    version = "1.0.0"

    repositories {
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/spring")
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "io.spring.dependency-management")

    extensions.configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.12")
            mavenBom("com.baomidou:mybatis-plus-bom:3.5.16")
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.4")
            mavenBom("com.alibaba.cloud:spring-cloud-alibaba-dependencies:2023.0.3.2")
        }
    }

    afterEvaluate {
        dependencies {
            "implementation"("org.projectlombok:lombok:1.18.36")
            "implementation"("org.apache.fesod:fesod-sheet:2.0.2-incubating")
            "compileOnly"("org.projectlombok:lombok:1.18.36")
            "testImplementation"("org.projectlombok:lombok:1.18.36")
        }
        if (plugins.hasPlugin("org.jetbrains.kotlin.kapt")) {
            dependencies {
                "kapt"("org.mapstruct:mapstruct-processor:1.6.3")
                "kapt"("org.projectlombok:lombok-mapstruct-binding:0.2.0")
            }
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.encoding = "UTF-8"
    }
}
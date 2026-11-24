pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/spring-plugin")
        gradlePluginPortal()
    }
    plugins {
        kotlin("kapt") version "2.1.20"
        kotlin("plugin.lombok") version "2.1.20"
    }
}
rootProject.name = "remix_kotlin"

include("system")
include("common")
include("common:common-core")
include("common:common-web")

include("common:common-redis")
include("auth")
include("api")
include("api:api-system")
include("gateway")
include("analysis")
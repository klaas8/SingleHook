pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
        maven("https://androidide.com/maven/releases")
        maven("https://androidide.com/maven/snapshots")
        maven("https://maven.aliyun.com/repository/apache-snapshots/")
        maven("https://maven.aliyun.com/repository/central/")
        maven("https://maven.aliyun.com/repository/google/")
        maven("https://maven.aliyun.com/repository/gradle-plugin/")
        maven("https://maven.aliyun.com/repository/jcenter/")
        maven("https://maven.aliyun.com/repository/spring/")
        maven("https://maven.aliyun.com/repository/spring-plugin/")
        maven("https://maven.aliyun.com/repository/releases/")
        maven("https://maven.aliyun.com/repository/snapshots/")
        maven("https://maven.aliyun.com/repository/grails-core/")
        maven("https://maven.aliyun.com/repository/mapr-public/")
        maven("https://maven.aliyun.com/repository/staging-alpha/")
        maven("https://maven.aliyun.com/repository/staging-alpha-group/")
        maven("https://maven.aliyun.com/repository/public/")
        maven("https://clojars.org/repo/")
        maven("https://jitpack.io/")
        maven("https://api.xposed.info/")
        maven("https://repo1.maven.org/maven2/")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
        maven("https://androidide.com/maven/releases")
        maven("https://androidide.com/maven/snapshots")
        maven("https://maven.aliyun.com/repository/apache-snapshots/")
        maven("https://maven.aliyun.com/repository/central/")
        maven("https://maven.aliyun.com/repository/google/")
        maven("https://maven.aliyun.com/repository/gradle-plugin/")
        maven("https://maven.aliyun.com/repository/jcenter/")
        maven("https://maven.aliyun.com/repository/spring/")
        maven("https://maven.aliyun.com/repository/spring-plugin/")
        maven("https://maven.aliyun.com/repository/releases/")
        maven("https://maven.aliyun.com/repository/snapshots/")
        maven("https://maven.aliyun.com/repository/grails-core/")
        maven("https://maven.aliyun.com/repository/mapr-public/")
        maven("https://maven.aliyun.com/repository/staging-alpha/")
        maven("https://maven.aliyun.com/repository/staging-alpha-group/")
        maven("https://maven.aliyun.com/repository/public/")
        maven("https://clojars.org/repo/")
        maven("https://jitpack.io/")
        maven("https://api.xposed.info/")
        maven("https://repo1.maven.org/maven2/")
    }
}

rootProject.name = "hook"

include(":app")
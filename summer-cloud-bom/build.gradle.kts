import top.bettercode.summer.gradle.plugin.publish.AbstractPublishPlugin

plugins {
    `java-platform`
    `maven-publish`
}

javaPlatform {
    allowDependencies()
}

dependencies {
//https://github.com/alibaba/spring-cloud-alibaba/releases

    api(platform("com.alibaba.cloud:spring-cloud-alibaba-dependencies:2.2.5.RELEASE"))
    api(platform("org.springframework.cloud:spring-cloud-dependencies:Hoxton.SR8"))
    api(platform(project(":summer-bom")))

    constraints {
    }
}

publishing {
    AbstractPublishPlugin.Companion.conifgRepository(project, this)

    publications {
        create<MavenPublication>("mavenPlatform") {
            from(components["javaPlatform"])
        }
    }
}





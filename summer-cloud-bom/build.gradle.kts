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

    api(enforcedPlatform("com.alibaba.cloud:spring-cloud-alibaba-dependencies:2021.0.5.0"))
    api(enforcedPlatform("org.springframework.cloud:spring-cloud-dependencies:2021.0.5"))
    api(platform(project(":summer-bom")))

    constraints {
        api("com.alibaba:druid-spring-boot-starter:1.2.16")
        api("org.mybatis.spring.boot:mybatis-spring-boot-starter:2.3.0")
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





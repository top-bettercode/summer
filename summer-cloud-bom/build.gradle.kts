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
        api("com.alibaba:druid-spring-boot-starter:1.2.16")
        api("org.mybatis.spring.boot:mybatis-spring-boot-starter:2.1.4")
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





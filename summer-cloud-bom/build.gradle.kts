import top.bettercode.summer.gradle.plugin.publish.AbstractPublishPlugin

plugins {
    `java-platform`
    `maven-publish`
}

javaPlatform {
    allowDependencies()
}

dependencies {
//https://sca.aliyun.com/docs/2021/overview/version-explain/
//https://github.com/alibaba/spring-cloud-alibaba/releases
    //https://repo1.maven.org/maven2/com/alibaba/cloud/spring-cloud-alibaba-dependencies/2.2.5.RELEASE/spring-cloud-alibaba-dependencies-2.2.5.RELEASE.pom
    api(platform("com.alibaba.cloud:spring-cloud-alibaba-dependencies:2.2.5.RELEASE"))
    //https://repo1.maven.org/maven2/org/springframework/cloud/spring-cloud-dependencies/Hoxton.SR8/spring-cloud-dependencies-Hoxton.SR8.pom
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





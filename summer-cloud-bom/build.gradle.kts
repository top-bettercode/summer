import top.bettercode.summer.gradle.plugin.publish.AbstractPublishPlugin

plugins {
    `java-platform`
    `maven-publish`
}

javaPlatform {
    allowDependencies()
}

dependencies {
//https://sca.aliyun.com/docs/2023/overview/version-explain/
//https://github.com/alibaba/spring-cloud-alibaba/releases
    //https://repo1.maven.org/maven2/com/alibaba/cloud/spring-cloud-alibaba-dependencies/2023.0.1.0/spring-cloud-alibaba-dependencies-2023.0.1.0.pom
    api(platform("com.alibaba.cloud:spring-cloud-alibaba-dependencies:2023.0.1.0"))
    //https://repo1.maven.org/maven2/org/springframework/cloud/spring-cloud-dependencies/2023.0.1/spring-cloud-dependencies-2023.0.1.pom
    api(platform("org.springframework.cloud:spring-cloud-dependencies:2023.0.1"))
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





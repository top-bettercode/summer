import top.bettercode.summer.gradle.plugin.publish.AbstractPublishPlugin

plugins {
    `java-platform`
    `maven-publish`
}

javaPlatform {
    allowDependencies()
}

dependencies {
//https://docs.gradle.org/7.5.1/userguide/compatibility.html
//https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-dependencies/2.6.13/spring-boot-dependencies-2.6.13.pom

    api(platform("org.springframework.boot:spring-boot-dependencies:2.6.13"))
    constraints {
        api("org.jetbrains.kotlin:kotlin-gradle-plugin:${KotlinVersion.CURRENT}")
        api("org.jetbrains.kotlin:kotlin-allopen:${KotlinVersion.CURRENT}")

        api("org.springframework.boot:spring-boot-gradle-plugin:2.6.13")

        api("org.jetbrains.dokka:kotlin-as-java-plugin:1.7.20")
        api("org.jetbrains.dokka:dokka-gradle-plugin:1.7.20")

        api("com.gradle.publish:plugin-publish-plugin:1.2.1")
        api("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.30.0")

        api("org.xerial:sqlite-jdbc:3.46.0.0")
        api("mysql:mysql-connector-java:8.0.32")
        api("com.oracle.database.jdbc:ojdbc8:21.9.0.0")
        api("com.oracle.database.jdbc:ucp:21.9.0.0")
        api("com.oracle.database.security:oraclepki:21.9.0.0")
        api("com.oracle.database.security:osdt_core:21.9.0.0")
        api("com.oracle.database.security:osdt_cert:21.9.0.0")

        api("org.asciidoctor:asciidoctorj:2.5.10")
        //高版本asciidoctorj-diagram要求JDK11
        api("org.asciidoctor:asciidoctorj-diagram:2.2.7")
        api("org.asciidoctor:asciidoctorj-pdf:2.3.9")

        api("org.dhatim:fastexcel:0.15.7")
        api("org.dhatim:fastexcel-reader:0.15.7")
        api("org.apache.poi:poi-ooxml:5.2.3")
        api("org.apache.poi:poi-scratchpad:5.2.3")

        api("org.mybatis:mybatis:3.5.14")
        api("org.mybatis:mybatis-spring:2.1.1")

        api("com.querydsl:querydsl-apt:5.0.0:jpa")

        api("com.github.jsqlparser:jsqlparser:4.6")

        api("org.apache.logging.log4j:log4j-api:2.20.0")
        api("org.apache.logging.log4j:log4j-core:2.20.0")
        api("org.apache.logging.log4j:log4j-to-slf4j:2.20.0")

        api("net.logstash.logback:logstash-logback-encoder:7.3")

        api("com.auth0:java-jwt:4.4.0")
        api("org.bouncycastle:bcprov-jdk18on:1.76")
        api("com.github.ulisesbocchio:jasypt-spring-boot-starter:3.0.5")

        api("com.tencentcloudapi:tencentcloud-sdk-java:3.1.865")
        api("com.qcloud:vod_api:2.1.5")

        api("commons-codec:commons-codec:1.16.0")
        api("org.json:json:20230618")
        api("org.javassist:javassist:3.29.2-GA")
        api("org.dom4j:dom4j:2.1.4")
        api("org.jsoup:jsoup:1.16.1")
        api("org.atteo:evo-inflector:1.3")
        api("com.github.axet:kaptcha:0.0.9")
        api("com.github.stuxuhai:jpinyin:1.1.8")
        api("net.sourceforge.plantuml:plantuml:1.2023.11")
        api("javax.mail:mail:1.4.7")
        api("com.github.easonjim:com.sap.conn.jco.sapjco3:3.0.11")
        api("com.google.code.findbugs:annotations:3.0.1")

        api("net.java.dev.jna:jna:5.13.0")
        api("com.squareup.okhttp3:okhttp:4.11.0")
        api("com.github.wechatpay-apiv3:wechatpay-java:0.2.11")
        api("com.jcraft:jsch:0.1.55")
        api("com.google.ortools:ortools-java:9.10.4067")
        api("com.gurobi:gurobi:11.0.0")

        api("io.swagger:swagger-annotations:1.6.12")

        api(project(":env"))
        api(project(":web"))
        api(project(":data-jpa"))
        api(project(":security"))
        api(project(":test"))

        api(project(":dist-plugin"))
        api(project(":summer-plugin"))
        api(project(":publish-plugin"))

        api(project(":tools:generator"))
        api(project(":tools:tools"))
        api(project(":tools:excel"))
        api(project(":tools:ueditor"))
        api(project(":tools:configuration-processor"))
        api(project(":tools:optimal"))
        api(project(":tools:optimal-gurobi"))
        api(project(":tools:optimal-ortools"))
        api(project(":tools:recipe"))

        api(project(":clients:weixin"))
        api(project(":clients:sms"))
        api(project(":clients:mobile-query"))
        api(project(":clients:rapidauth"))
        api(project(":clients:weather"))
        api(project(":clients:jpush"))
        api(project(":clients:qvod"))
        api(project(":clients:amap"))
        api(project(":clients:pay"))

        api(project(":natives:sap"))
        api(project(":natives:optimal-copt"))
        api(project(":natives:optimal-cplex"))
//        api(project(":natives:ctpapi"))

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





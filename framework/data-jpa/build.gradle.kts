plugins { `java-library` }

dependencies {
    api(project(":framework:web"))

    //data
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    api("org.mybatis:mybatis")
    api("org.mybatis:mybatis-spring")
    api("com.github.pagehelper:pagehelper")

    compileOnly("com.querydsl:querydsl-jpa")
    testImplementation("com.querydsl:querydsl-jpa")
//    testAnnotationProcessor("com.querydsl:querydsl-apt:4.3.1:jpa")
//    testAnnotationProcessor("jakarta.persistence:jakarta.persistence-api")

    testImplementation("com.h2database:h2")
}


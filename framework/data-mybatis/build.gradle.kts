plugins { `java-library` }
dependencies {
    api(project(":framework:web"))

    //data
    api("com.baomidou:mybatis-plus")
    api("com.baomidou:mybatisplus-spring-boot-starter")
    testImplementation("com.h2database:h2")
}



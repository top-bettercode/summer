plugins {
    `java-library`
}

dependencies {
    api("org.rationalityfrontline:kevent:2.1.2")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
//    api("org.rationalityfrontline.ktrader:ktrader-datatype:1.1.0")
    api(project(":tools:ktrader-datatype"))
}
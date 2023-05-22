package top.bettercode.summer.tools.configuration.processor

// 定义一个自定义注解，用于标记要替换yaml配置文件中的字符的类
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class ConfigProfile(
)
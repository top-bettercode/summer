package top.bettercode.summer.web.resolver

/**
 * 千克字符串(1.2)转克长整型(1200)注解
 *
 * @author Peter Wu
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class KilogramToGram

package top.bettercode.summer.data.jpa.support

/**
 * @author Peter Wu
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.ANNOTATION_CLASS)
@MustBeDocumented
@QuerySize(1)
annotation class QueryFirst  
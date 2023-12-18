package top.bettercode.summer.tools.sap.annotation

/**
 * JavaBean对应SAP接口服务中的参数名称
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class SapField( /*
   * SAP方法中对应的参数名称
   */
                           val value: String)

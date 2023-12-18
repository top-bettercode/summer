package top.bettercode.summer.tools.sap.annotation

/**
 * 定义SAP输入参数为结构化数据对象, 对应SAP中的 JCoStructure
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class SapStructure( /*
   * SAP方法中对应的结构化对象参数名称
   */
                               val value: String)

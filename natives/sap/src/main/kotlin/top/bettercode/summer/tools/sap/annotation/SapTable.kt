package top.bettercode.summer.tools.sap.annotation

/**
 * 定义SAP输入参数列表（多行数据），映射JcoTable
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class SapTable( /*
   * SAP方法中对应的table参数名称
   */
                           val value: String)

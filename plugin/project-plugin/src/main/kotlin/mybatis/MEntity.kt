import cn.bestwu.generator.DataType
import cn.bestwu.generator.dom.java.JavaType

/**
 * @author Peter Wu
 */
class MEntity : MModuleJavaGenerator() {

    override var cover: Boolean = true
    override val type: JavaType
        get() = entityType

    override fun content() {
        clazz {
            val tableName =
                if (schema.isNullOrBlank() || schema == extension.datasource.schema) tableName else "$schema.$tableName"
            annotation("@com.baomidou.mybatisplus.annotations.TableName(\"$tableName\")")
            if (enable(
                    "keySequence",
                    false
                ) && extension.dataType != DataType.PUML || table.sequenceStartWith != null && isOracleDatasource
            )
                annotation("@com.baomidou.mybatisplus.annotations.KeySequence(\"${tableName}_S\")")
            javadoc {
                +"/**"
                +" * $remarks 对应表名：$tableName"
                +" */"
            }
            implement {
                +"java.io.Serializable"
            }
            serialVersionUID()
            columns.forEach {
                //field
                import(it.javaType)
                field(it.javaName, it.javaType) {
                    if (it.isPrimary) {
                        if (enable("idWorkerStr", false))
                            annotation("@com.baomidou.mybatisplus.annotations.TableId(value = \"${it.columnName}\", type = com.baomidou.mybatisplus.enums.IdType.ID_WORKER_STR)")
                        else
                            annotation("@com.baomidou.mybatisplus.annotations.TableId(\"${it.columnName}\")")
                    } else {
                        if (it.javaName == "lastModifiedDate")
                            annotation("@com.baomidou.mybatisplus.annotations.TableField(value = \"${it.columnName}\",el = \"${it.javaName},jdbcType=${it.jdbcType}\", update = \"${if (isOracleDatasource) "sysdate" else "now()"}\")")
                        else
                            annotation("@com.baomidou.mybatisplus.annotations.TableField(value = \"${it.columnName}\",el = \"${it.javaName},jdbcType=${it.jdbcType}\")")
                    }
                    if (it.isSoftDelete) {
                        annotation("@com.baomidou.mybatisplus.annotations.TableLogic")
                    }
                    if (it.remarks.isNotBlank())
                        if (it.remarks.isNotBlank() || !it.columnDef.isNullOrBlank())
                            javadoc {
                                +"/**"
                                +" * ${it.remarks}${if (it.columnDef.isNullOrBlank()) "" else " 默认值：${it.columnDef}"}"
                                +" */"
                            }
                }
            }

            //constructor no args
            constructor {}

            columns.forEach {
                //getter
                method("get${it.javaName.capitalize()}", it.javaType) {
                    if (it.remarks.isNotBlank() || !it.columnDef.isNullOrBlank())
                        javadoc {
                            +"/**"
                            +" * @return ${(if (it.remarks.isBlank()) "" else it.remarks)}${if (it.columnDef.isNullOrBlank()) "" else " 默认值：${it.columnDef}"}"
                            +" */"
                        }
                    +"return ${it.javaName};"
                }
                //setter
                method("set${it.javaName.capitalize()}") {
                    if (it.remarks.isNotBlank() || !it.columnDef.isNullOrBlank())
                        javadoc {
                            +"/**"
                            +" * @param  ${if (it.remarks.isBlank()) "" else "${it.javaName} ${it.remarks}"}${if (it.columnDef.isNullOrBlank()) "" else " 默认值：${it.columnDef}"}"
                            +" */"
                        }
                    parameter {
                        type = it.javaType
                        name = it.javaName
                    }
                    +"this.${it.javaName} = ${it.javaName};"
                }
            }
        }
    }
}
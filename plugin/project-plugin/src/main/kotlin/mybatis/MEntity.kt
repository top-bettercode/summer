import top.bettercode.generator.DataType
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.JavaVisibility
import top.bettercode.generator.dom.java.element.Parameter

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
                +" * $remarks 对应数据库表名：$tableName"
                +" */"
            }
            implement {
                +"java.io.Serializable"
            }
            serialVersionUID()

            //constructor no args
            constructor {}
            //constructor with id
            if (primaryKeys.size == 1)
                constructor(Parameter(primaryKeyName, primaryKey.javaType)) {
                    +"this.${primaryKeyName} = ${primaryKeyName};"
                }

            field("TABLE_NAME", JavaType.stringInstance, "\"${tableName}\"") {
                visibility = JavaVisibility.PUBLIC
                isStatic = true
                isFinal = true
                javadoc {
                    +"/**"
                    +" * 对应数据库表名"
                    +" */"
                }
            }

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

            columns.forEach {
                //getter
                method("get${it.javaName.capitalize()}", it.javaType) {
                    if (it.remarks.isNotBlank() || !it.columnDef.isNullOrBlank())
                        javadoc {
                            +"/**"
                            +" * @return ${(it.remarks.ifBlank { "" })}${if (it.columnDef.isNullOrBlank()) "" else " 默认值：${it.columnDef}"}"
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

            //equals
            import("java.util.Objects")
            method(
                "equals",
                JavaType.booleanPrimitiveInstance,
                Parameter("o", JavaType.objectInstance)
            ) {
                annotation("@Override")
                +"if (this == o) {"
                +"return true;"
                +"}"
                +"if (!(o instanceof ${className})) {"
                +"return false;"
                +"}"
                +"$className that = (${className}) o;"
                val size = columns.size
                columns.forEachIndexed { index, column ->
                    when (index) {
                        0 -> {
                            +"return Objects.equals(${column.javaName}, that.${column.javaName}) &&"
                        }
                        size - 1 -> {
                            +"    Objects.equals(${column.javaName}, that.${column.javaName});"
                        }
                        else -> {
                            +"    Objects.equals(${column.javaName}, that.${column.javaName}) &&"
                        }
                    }
                }
            }

            //hashCode
            method("hashCode", JavaType.intPrimitiveInstance) {
                annotation("@Override")
                +"return Objects.hash(${columns.joinToString(", ") { it.javaName }});"
            }

            //toString
            method("toString", JavaType.stringInstance) {
                annotation("@Override")
                +"return \"${className}{\" +"
                if (hasPrimaryKey) {
                    +"    \"${primaryKeyName}='\" + $primaryKeyName + '\\'' +"
                }
                otherColumns.forEachIndexed { i, it ->
                    +"    \"${if (i > 0) ", " else ""}${it.javaName}=${if (it.javaType == JavaType.stringInstance) "'" else ""}\" + ${it.javaName} ${if (it.javaType == JavaType.stringInstance) "+ '\\'' " else ""}+"
                }
                +"    '}';"
            }
        }
    }
}
package cn.bestwu.generator.dsl.def

import cn.bestwu.generator.database.domain.Table
import cn.bestwu.generator.dom.java.JavaType
import cn.bestwu.generator.dom.java.StringOperator
import cn.bestwu.generator.dom.java.element.JavaElement.Companion.indent
import cn.bestwu.generator.dsl.JavaGenerator

/**
 * @author Peter Wu
 */
open class CrudDao : JavaGenerator() {

    override var cover: Boolean = true
    override val name: String
        get() = "dao.crud.I${className}CrudDao"

    private var withResults = true

    override fun content() {
        interfaze {
            javadoc {
                +"/**"
                +" * $remarks 数据层"
                +" */"
            }
            import("org.apache.ibatis.type.JdbcType")
            method("create", JavaType.intPrimitiveInstance) {
                annotation {
                    val sb = StringBuilder("@Insert({")
                    sb.appendln()
                    indent(sb, 3)
                    sb.append("\"INSERT INTO $tableName (")
                    val size = columns.size
                    var length = sb.length
                    columns.forEachIndexed { i, it ->
                        if (!it.isPrimary || isOracleDatasource) {
                            sb.append(it.columnName)
                            if (i != size - 1)
                                sb.append(",")
                            length += it.columnName.length
                            length = wrapLine(length, sb)
                        }
                    }
                    sb.append((") VALUES ("))
                    columns.forEachIndexed { i, it ->
                        if (!it.isPrimary || isOracleDatasource) {
                            sb.append("#{${it.javaName},jdbcType=${it.jdbcType}}")
                            length += "#{${it.javaName},jdbcType=${it.jdbcType}}".length
                            if (i != size - 1) {
                                sb.append((","))
                                length = wrapLine(length, sb)
                            } else {
                                sb.append(")\"")
                            }
                        }
                    }
                    sb.appendln()
                    indent(sb, 1)
                    sb.append("})")
                    +sb.toString()
                    val selectKeyString = selectKey(table)
                    if (selectKeyString.isNotBlank() && primaryKey.javaType != JavaType.stringInstance) {
                        +"@SelectKey(statement = \"$selectKeyString\", resultType = ${primaryKey.javaType.shortName}.class, before = ${if (isOracleDatasource) "true" else "false"}, keyProperty = \"${primaryKey.javaName}\")"
                    }
                    import("org.apache.ibatis.annotations.Insert")
                    import("org.apache.ibatis.annotations.SelectKey")
                }
                parameter(domainJavaType, "entity")
            }

            method("update", JavaType.intPrimitiveInstance) {
                annotation {
                    val sb = StringBuilder("@Update({")
                    sb.appendln()
                    indent(sb, 3)
                    sb.append("\"UPDATE $tableName SET ")
                    val size = columns.size
                    var length = sb.length
                    columns.forEachIndexed { i, it ->
                        if (!it.isPrimary) {
                            sb.append("${it.columnName}=#{${it.javaName},jdbcType=${it.jdbcType}}")
                            if (i != size - 1)
                                sb.append(",")
                            length += "${it.columnName}=#{${it.javaName},jdbcType=${it.jdbcType}}".length
                            length = wrapLine(length, sb)
                        }
                    }
                    sb.append(" WHERE ${primaryKey.columnName} = #{${primaryKey.javaName}}\"")
                    sb.appendln()
                    indent(sb, 1)
                    sb.append("})")
                    +sb.toString()
                    import("org.apache.ibatis.annotations.Update")
                }
                parameter(domainJavaType, "entity")
            }

            method("delete", JavaType.intPrimitiveInstance) {
                annotation {
                    +"@Delete(\"DELETE FROM $tableName WHERE ${primaryKey.columnName} = #{${primaryKey.javaName}}\")"
                    import("org.apache.ibatis.annotations.Delete")
                }
                parameter(primaryKey.javaType, primaryKey.javaName)
            }
            import("org.apache.ibatis.annotations.Select")
            import("org.apache.ibatis.annotations.Results")
            import("org.apache.ibatis.annotations.Result")
            method("exist", JavaType.booleanPrimitiveInstance) {
                annotation {
                    +"@Select(\"SELECT COUNT(${primaryKey.columnName}) FROM $tableName WHERE ${primaryKey.columnName} = #{${primaryKey.javaName}}\")"
                }
                parameter(primaryKey.javaType, primaryKey.javaName)
            }
            method("findAll", JavaType.listInstance.typeArgument(domainJavaType)) {
                annotation {
                    +"@Select(\"SELECT * FROM $tableName\")"
                    if (withResults)
                        anotatedResults(this)
                }
            }
            method("findOne", domainJavaType) {
                annotation {
                    +"@Select(\"SELECT * FROM $tableName WHERE ${primaryKey.columnName} = #{${primaryKey.javaName}}\")"
                    if (withResults)
                        anotatedResults(this)
                }
                parameter(primaryKey.javaType, primaryKey.javaName)
            }
        }
    }

    private fun wrapLine(length: Int, sb: StringBuilder): Int {
        var length1 = length
        if (length1 > 80) {
            length1 = 0
            sb.append('"')
            sb.append(",")
            sb.appendln()
            indent(sb, 3)
            sb.append('"')
        }
        return length1
    }

    private fun anotatedResults(annotation: StringOperator) {
        annotation.run {
            +"@Results({"
            val iterator = columns.iterator()
            while (iterator.hasNext()) {
                val column = iterator.next()
                2 + "@Result(column = \"${column.columnName}\", property = \"${column.javaName}\", jdbcType = JdbcType.${column.jdbcType}${if (primaryKeys.contains(column)) ", id = true" else ""})${if (iterator.hasNext() || iterator.hasNext()) "," else ""}"
            }
            +"})"
        }
    }

    fun withResults(withResults: Boolean): CrudDao {
        this.withResults = withResults
        return this
    }

    private var selectKey: (Table) -> String = {
        //CREATE SEQUENCE SEQ_XXX_ID INCREMENT BY 1 START WITH 1
        if (isOracleDatasource) {
            "SELECT SEQ_${if (it.tableName.length > 23) it.tableName.substring(0, 23) else it.tableName}_ID.NEXTVAL FROM DUAL"
        } else {
            "SELECT LAST_INSERT_ID() AS ${primaryKey.columnName}"
        }
    }

    fun selectKey(selectKey: (Table) -> String): CrudDao {
        this.selectKey = selectKey
        return this
    }

}
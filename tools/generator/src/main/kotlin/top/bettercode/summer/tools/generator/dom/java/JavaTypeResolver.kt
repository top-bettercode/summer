package top.bettercode.summer.tools.generator.dom.java

import top.bettercode.summer.tools.generator.database.entity.Column
import top.bettercode.summer.tools.lang.util.JavaType
import top.bettercode.summer.tools.lang.util.JavaTypeResolver
import top.bettercode.summer.tools.lang.util.JavaTypeResolver.forceBigDecimals
import top.bettercode.summer.tools.lang.util.JavaTypeResolver.forceIntegers
import top.bettercode.summer.tools.lang.util.JavaTypeResolver.useJSR310Types
import java.sql.Types

/**
 * @author Peter Wu
 */
object ColumnJavaTypeResolver {

    fun calculateJavaType(column: Column): JavaType {
        var answer: JavaType
        val information = calculateJdbcTypeInformation(column)
        answer = information.javaType
        answer = overrideDefaultType(column, answer)

        return if (forceIntegers && (JavaType(java.lang.Short::class.java.name) == answer || JavaType(
                java.lang.Byte::class.java.name
            ) == answer)
        ) {
            JavaType(java.lang.Integer::class.java.name)
        } else
            answer
    }

    fun calculateJdbcTypeName(column: Column): String {
        val jdbcTypeInformation = calculateJdbcTypeInformation(column)
        return jdbcTypeInformation.jdbcTypeName
    }

    private fun calculateJdbcTypeInformation(column: Column): JavaTypeResolver.JdbcTypeInformation {
        return JavaTypeResolver.type(calculateDataType(column))
            ?: throw IllegalStateException("无法计算${column.typeName}对应的Java类型，请在jdbcTypeName.properties中添加映射")
    }

    private fun overrideDefaultType(column: Column, defaultType: JavaType): JavaType {
        var answer = defaultType

        when (calculateDataType(column)) {
            Types.BIT -> answer = calculateBitReplacement(column, defaultType)
            Types.DECIMAL, Types.NUMERIC -> answer =
                calculateBigDecimalReplacement(column, defaultType)

            Types.DATE -> answer = calculateDateType(defaultType)
            Types.TIME -> answer = calculateTimeType(defaultType)
            Types.TIMESTAMP -> answer = calculateTimestampType(defaultType)
            else -> {
            }
        }

        return answer
    }

    private fun calculateDateType(defaultType: JavaType): JavaType {
        return if (useJSR310Types) {
            JavaType("java.time.LocalDate")
        } else {
            defaultType
        }
    }

    private fun calculateTimeType(defaultType: JavaType): JavaType {
        return if (useJSR310Types) {
            JavaType("java.time.LocalTime")
        } else {
            defaultType
        }
    }

    private fun calculateTimestampType(defaultType: JavaType): JavaType {
        return if (useJSR310Types) {
            JavaType("java.time.LocalDateTime")
        } else {
            defaultType
        }
    }

    private fun calculateDataType(column: Column) =
        JavaTypeResolver.calculateDataType(column.typeName) ?: column.dataType
        ?: throw IllegalStateException("无法计算${column.typeName}对应的dataType类型，请在jdbcTypeName.properties中添加映射")


    private fun calculateBitReplacement(column: Column, defaultType: JavaType): JavaType {
        return if (column.columnSize > 1) {
            JavaType("byte[]")
        } else {
            defaultType
        }
    }

    private fun calculateBigDecimalReplacement(column: Column, defaultType: JavaType): JavaType {
        return if (column.decimalDigits > 0 || column.columnSize > 20 || forceBigDecimals) {
            defaultType
        } else if (column.columnSize > 11 || column.columnSize == 0) {
            JavaType(java.lang.Long::class.java.name)
        } else if (column.columnSize > 4) {
            JavaType(java.lang.Integer::class.java.name)
        } else {
            JavaType(java.lang.Short::class.java.name)
        }
    }

}

import java.io.PrintWriter

/**
 * @author Peter Wu
 */
open class MapperXml : ModuleJavaGenerator() {
    override fun content() {
    }

    override fun output(printWriter: PrintWriter) {
        printWriter.println(
            """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="${repositoryType.fullyQualifiedNameWithoutTypeParameters}">
"""
        )
        if (compositePrimaryKey) {
            printWriter.println("""  <resultMap type="${entityType.fullyQualifiedNameWithoutTypeParameters}" id="${entityName}Map">""")
            primaryKeys.forEach {
                printWriter.println("    <result property=\"${primaryKeyName}.${it.javaName}\" column=\"${it.columnName}\"/>")
            }
            otherColumns.forEach {
                printWriter.println("    <result property=\"${it.javaName}\" column=\"${it.columnName}\"/>")
            }
            printWriter.println("""  </resultMap>""")

        }
        printWriter.println(
            """

</mapper>""".trimIndent()
        )

    }

    override val resources: Boolean
        get() = true

    override val name: String
        get() = "${repositoryType.fullyQualifiedNameWithoutTypeParameters.replace(".", "/")}.xml"

}
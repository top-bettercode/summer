package jpa.unit

import ModuleJavaGenerator
import top.bettercode.generator.dom.java.element.FileUnit

/**
 * @author Peter Wu
 */
val mapperXml: ModuleJavaGenerator.(FileUnit) -> Unit = { unit ->
    unit.apply {
        +"""<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="${repositoryType.fullyQualifiedNameWithoutTypeParameters}">
"""

        if (isCompositePrimaryKey) {
            +"""  <resultMap type="${entityType.fullyQualifiedNameWithoutTypeParameters}" id="${entityName}Map">"""
            primaryKeys.forEach {
                +("    <result property=\"${primaryKeyName}.${it.javaName}\" column=\"${it.columnName}\"/>")
            }
            otherColumns.forEach {
                +("    <result property=\"${it.javaName}\" column=\"${it.columnName}\"/>")
            }
            +("""  </resultMap>""")

        }
        +
        """

</mapper>""".trimIndent()

    }
}
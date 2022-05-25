package jpa.unit

import ProjectGenerator
import top.bettercode.generator.dom.unit.FileUnit

/**
 * @author Peter Wu
 */
val mapperXml: ProjectGenerator.(FileUnit) -> Unit = { unit ->
    unit.apply {
        +"""<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="${repositoryType.fullyQualifiedNameWithoutTypeParameters}">
"""

        +
        """

</mapper>""".trimIndent()

    }
}
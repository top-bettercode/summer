import java.io.PrintWriter

/**
 * @author Peter Wu
 */
open class DaoXml : MModuleJavaGenerator() {
    override val resources: Boolean
        get() = true

    override fun output(printWriter: PrintWriter) {
        printWriter.println("""<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="$daoType">""")
        
        printWriter.println("""
</mapper>""")
    }

    override val dir: String = "src/main/resources/mapper"
    override val name: String
        get() = if (extension.userModule && module.isNotBlank()) {
            "$module/$projectEntityName.xml"
        } else {
            "${projectEntityName}.xml"
        }

    override fun content() {
    }


}
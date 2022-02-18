/**
 * @author Peter Wu
 */
open class DaoXml : MProjectGenerator() {

    override fun content() {
        file(daoXml, isResourcesFile = true) {


            +"""<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="$daoType">"""

            +"""
</mapper>"""
        }
    }

}
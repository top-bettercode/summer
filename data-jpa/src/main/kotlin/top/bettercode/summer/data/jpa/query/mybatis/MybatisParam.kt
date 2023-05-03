package top.bettercode.summer.data.jpa.query.mybatis

import org.apache.ibatis.mapping.BoundSql
import top.bettercode.summer.data.jpa.support.Size

/**
 * @author Peter Wu
 */
class MybatisParam(
        val boundSql: BoundSql, val parameterObject: Any?,
        val size: Size?
)

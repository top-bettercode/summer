package top.bettercode.summer.data.jpa.query.mybatis

import jakarta.persistence.Query


/**
 * @author Peter Wu
 */
class MybatisQuery(
    val query: Query,
    val mybatisParam: MybatisParam
) : Query by query
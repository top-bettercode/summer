package top.bettercode.summer.data.jpa.query.mybatis

import javax.persistence.TupleElement

class NativeTupleElementImpl<X>(private val javaType: Class<out X>, private val alias: String) : TupleElement<X> {
    override fun getJavaType(): Class<out X> {
        return javaType
    }

    override fun getAlias(): String {
        return alias
    }
}
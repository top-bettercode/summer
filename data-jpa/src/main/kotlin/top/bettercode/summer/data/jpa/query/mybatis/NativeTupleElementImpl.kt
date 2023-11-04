package top.bettercode.summer.data.jpa.query.mybatis

import org.apache.ibatis.type.JdbcType
import org.hibernate.type.descriptor.sql.JdbcTypeJavaClassMappings
import javax.persistence.TupleElement

class NativeTupleElementImpl<X>(private val javaType: Class<out X>, private val alias: String, private val value: X?) : TupleElement<X> {
    override fun getJavaType(): Class<out X> {
        return javaType
    }

    override fun getAlias(): String {
        return alias
    }

    fun getJdbcType(): JdbcType {
        return if (value == null) {
            JdbcType.NULL
        } else {
            val type = JdbcTypeJavaClassMappings.INSTANCE.determineJdbcTypeCodeForJavaClass(javaType)
            JdbcType.forCode(type)
        }
    }

}
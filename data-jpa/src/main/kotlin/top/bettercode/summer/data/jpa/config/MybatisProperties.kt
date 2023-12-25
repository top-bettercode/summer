package top.bettercode.summer.data.jpa.config

import org.apache.ibatis.session.Configuration
import org.apache.ibatis.type.TypeHandler
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import java.util.*

/**
 * Configuration properties for MyBatis.
 *
 * @author Eddú Meléndez
 * @author Kazuki Shimizu
 */
@ConfigurationProperties(prefix = MybatisProperties.MYBATIS_PREFIX)
open class MybatisProperties {
    /**
     * Location of MyBatis xml config file.
     */
    var configLocation: String? = null

    /**
     * Locations of MyBatis mapper files.
     */
    var mapperLocations: Array<String> = arrayOf()

    /**
     * Packages to search type aliases. (Package delimiters are ",; \t\n")
     */
    var typeAliasesPackage: String? = null
    var typeAliases: Array<Class<*>> = arrayOf()

    /**
     * The super class for filtering type alias. If this not specifies, the MyBatis deal as type alias
     * all classes that searched from typeAliasesPackage.
     */
    var typeAliasesSuperType: Class<*>? = null

    /**
     * Packages to search for type handlers. (Package delimiters are ",; \t\n")
     */
    var typeHandlersPackage: String? = null
    var typeHandlerClasses: Array<Class<TypeHandler<*>>> = arrayOf()


    /**
     * Externalized properties for MyBatis configuration.
     */
    var configurationProperties: Properties? = null

    /**
     * 结果集转换是否使用 TupleTransformer,true时存在类型转换不兼容问题，如日期时间格式化
     */
    var useTupleTransformer = false

    /**
     * A Configuration object for customize default settings. If [.configLocation] is specified,
     * this property is not used.
     */
    @NestedConfigurationProperty
    var configuration: Configuration? = null

    companion object {
        const val MYBATIS_PREFIX = "summer.data.jpa.mybatis"
    }
}

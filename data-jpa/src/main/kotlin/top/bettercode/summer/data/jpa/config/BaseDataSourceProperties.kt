package top.bettercode.summer.data.jpa.config

import com.zaxxer.hikari.HikariDataSource

class BaseDataSourceProperties {
    //--------------------------------------------
    /**
     * JDBC URL of the database.
     */
    var url: String? = null

    /**
     * Login username of the database.
     */
    var username: String? = null

    /**
     * Login password of the database.
     */
    var password: String? = null

    /**
     * 配置 [@EnableJpaExtRepositories][EnableJpaExtRepositories] 的类
     */
    lateinit var extConfigClass: Class<*>

    /**
     * Locations of MyBatis mapper files.
     */
    var mapperLocations: Array<String> = arrayOf()

    var hikari: HikariDataSource? = null
}
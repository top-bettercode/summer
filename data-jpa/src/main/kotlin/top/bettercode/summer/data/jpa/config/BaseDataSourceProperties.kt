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
     * Locations of MyBatis mapper files.
     */
    var mapperLocations: Array<String> = arrayOf()

    var hikari: HikariDataSource? = null
}
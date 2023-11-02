package top.bettercode.summer.data.jpa.config

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties

class DataSourceExtProperties : DataSourceProperties() {
    /**
     * Locations of MyBatis mapper files.
     */
    var mapperLocations: Array<String> = arrayOf()

    /**
     * jpaProperties
     */
    var jpaProperties: Map<String, String?> = mutableMapOf()

}
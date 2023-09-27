package top.bettercode.summer.tools.generator

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 *
 * @author Peter Wu
 */
class DatabaseConfigurationTest {

    @Test
    fun getDbHostPort() {
        //mysql
        var dbHostPort = DatabaseConfiguration.getDbHostPort(DatabaseDriver.MYSQL, "jdbc:mysql://10.0.2.82:3306/qxbb?characterEncoding=utf8&useSSL=false")
        System.err.println(dbHostPort)
        Assertions.assertEquals(Pair("10.0.2.82", 3306), dbHostPort)
        dbHostPort = DatabaseConfiguration.getDbHostPort(DatabaseDriver.MYSQL, "jdbc:mysql://10.0.2.82/qxbb?characterEncoding=utf8&useSSL=false")
        System.err.println(dbHostPort)
        Assertions.assertEquals(Pair("10.0.2.82", 3306), dbHostPort)
        //oracle
        var dbHostPort2 = DatabaseConfiguration.getDbHostPort(DatabaseDriver.ORACLE, "jdbc:oracle:thin:@hollyora.wintrueholding.com:1521:hollycrm")
        System.err.println(dbHostPort2)
        Assertions.assertEquals(Pair("hollyora.wintrueholding.com", 1521), dbHostPort2)

        dbHostPort2 = DatabaseConfiguration.getDbHostPort(DatabaseDriver.ORACLE, "jdbc:oracle:thin:@hollyora.wintrueholding.com:hollycrm")
        System.err.println(dbHostPort2)
        Assertions.assertEquals(Pair("hollyora.wintrueholding.com", 1521), dbHostPort2)
    }
}
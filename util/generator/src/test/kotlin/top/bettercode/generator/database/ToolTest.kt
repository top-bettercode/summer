package top.bettercode.generator.database

import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.dom.java.Annotations
import org.junit.jupiter.api.Test

/**
 *
 * @author Peter Wu
 */
class ToolTest {

    @Test
    fun name() {
        val size = 22
        var array = arrayOf<Int>()
        (0 until size).map {
            array += it
        }
        println(array.joinToString())
        val map = mutableMapOf<Int, MutableList<Int>>()
        var i = 1
        array.forEach {
            if (i > 10) {
                i = 1
            }
            map.computeIfAbsent(i) { mutableListOf() }.add(it)
            i++
        }
        map.values.map {
            it.map { i ->
                print(i)
                print(" ")
            }
            println()
        }
    }

    @Test
    fun regex() {
        val s =
            "jdbc:mysql://127.0.0.1:3306/stopcar?useUnicode=true&characterEncoding=utf-8&useSSL=false"
        val s1 = "jdbc:mysql://127.0.0.1:3306/stopcar"
        System.err.println(s.replace("^.+\\/(.+)\\?.*$", "$1"))
        System.err.println(s1.replace("^.+\\/(.+).*$", "$1"))
        System.err.println(listOf("sd", "sd").distinct())
    }

    @Test
    fun javaName() {
        val extension = GeneratorExtension()
        extension.tablePrefixes = arrayOf("ls_")
        println(GeneratorExtension.javaName("activity_event"))
        println(extension.className("ls_activity_event"))
    }

    @Test
    fun annotation() {
        val regex = Annotations.regex
        val groups = regex.find("@Test")?.groupValues
        System.err.println(groups)
        org.junit.jupiter.api.Assertions.assertEquals("@Test", groups?.get(1))
        val groups1 = regex.find("@Test()")?.groupValues
        System.err.println(groups1)
        org.junit.jupiter.api.Assertions.assertEquals("@Test", groups1?.get(1))
        val groups2 = regex.find("@Test(\"\")")?.groupValues
        System.err.println(groups2)
        org.junit.jupiter.api.Assertions.assertEquals("@Test", groups2?.get(1))
        val groups3 = regex.find("@Test(\"dd\")")?.groupValues
        System.err.println(groups3)
        org.junit.jupiter.api.Assertions.assertEquals("@Test", groups3?.get(1))
        val groups4 = regex.find("@Test(value=\"dd\")")?.groupValues
        System.err.println(groups4)
        org.junit.jupiter.api.Assertions.assertEquals("@Test", groups4?.get(1))

        val ss = "@Results({\n" +
                " @Test()       @Result(column=\"id\", property=\"id\", jdbcType=JdbcType.BIGINT, id=true),\n" +
                "        @Result2(column=\"type\", property=\"type\", jdbcType=JdbcType.INTEGER),\n" +
                "        @Result1(column=\"ui_id\", property=\"uiId\", jdbcType=JdbcType.BIGINT),\n" +
                "        @Result(column=\"ui_nd\", property=\"uiNd\", jdbcType=JdbcType.VARCHAR),\n" +
                "        @Result(column=\"pi_id\", property=\"piId\", jdbcType=JdbcType.BIGINT),\n" +
                "        @Result(column=\"pi_name\", property=\"piName\", jdbcType=JdbcType.VARCHAR),\n" +
                "        @Result(column=\"area_code\", property=\"areaCode\", jdbcType=JdbcType.VARCHAR),\n" +
                "        @Result(column=\"car_code\", property=\"carCode\", jdbcType=JdbcType.VARCHAR),\n" +
                "        @Result(column=\"order_type\", property=\"orderType\", jdbcType=JdbcType.INTEGER),\n" +
                "        @Result(column=\"orderid\", property=\"orderid\", jdbcType=JdbcType.VARCHAR),\n" +
                "        @Result(column=\"ai_id\", property=\"aiId\", jdbcType=JdbcType.BIGINT),\n" +
                "        @Result(column=\"money\", property=\"money\", jdbcType=JdbcType.INTEGER),\n" +
                "        @Result(column=\"ctime\", property=\"ctime\", jdbcType=JdbcType.TIMESTAMP),\n" +
                "        @Result(column=\"state\", property=\"state\", jdbcType=JdbcType.INTEGER),\n" +
                "        @Result(column=\"utime\", property=\"utime\", jdbcType=JdbcType.TIMESTAMP),\n" +
                "        @Result(column=\"ai_money\", property=\"aiMoney\", jdbcType=JdbcType.INTEGER),\n" +
                "        @Result(column=\"note\", property=\"note\", jdbcType=JdbcType.VARCHAR),\n" +
                "        @Result(column=\"md5\", property=\"md5\", jdbcType=JdbcType.VARCHAR),\n" +
                "        @Result(column=\"send_unit\", property=\"sendUnit\", jdbcType=JdbcType.INTEGER),\n" +
                "        @Result(column=\"act_type\", property=\"actType\", jdbcType=JdbcType.INTEGER),\n" +
                "        @Result(column=\"method_name\", property=\"methodName\", jdbcType=JdbcType.VARCHAR),\n" +
                "        @Result(column=\"pay_source\", property=\"paySource\", jdbcType=JdbcType.INTEGER)\n" +
                "    })"
        ss.split(Annotations.splitRegex).forEach {
            val groupValues = regex.find(it)?.groupValues
            System.err.println(groupValues)
        }
        val sss =
            "@org.springframework.validation.annotation.Validated"
        sss.split(Annotations.splitRegex).forEach {
            val groupValues = regex.find(it)?.groupValues
            System.err.println(groupValues)
        }
//        org.junit.jupiter.api.Assertions.assertEquals("Test", groupValues?.get(1))
//        org.junit.jupiter.api.Assertions.assertEquals("(value=\"dd\")", groupValues?.get(2))
    }
}
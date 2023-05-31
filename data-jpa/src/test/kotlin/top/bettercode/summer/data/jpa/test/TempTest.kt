package top.bettercode.summer.data.jpa.test

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.data.jpa.repository.query.MybatisJpaQuery

/**
 *
 * @author Peter Wu
 */
class TempTest {

    val regex = MybatisJpaQuery.sqlValRegex
    val replacement = MybatisJpaQuery.sqlValReplacement

    @Test
    fun test() {
        val bef = """select first_name,
               last_name,
               @val := @val + 1 as version
        from t_user u,
             (select @val := 0) t
        where deleted = 0"""
        val res = """select first_name,
               last_name,
               @val \:= @val + 1 as version
        from t_user u,
             (select @val \:= 0) t
        where deleted = 0"""
        val res1 = bef.replace(regex, replacement)
        Assertions.assertEquals(res, res1)
        val res2 = res.replace(regex, replacement)
        Assertions.assertEquals(res, res2)
    }

    @Test
    fun test2() {
        val bef = """select first_name, last_name, @val := @val + 1 as version from t_user u, (select @val := 0) t where deleted = 0"""
        val res = """select first_name, last_name, @val \:= @val + 1 as version from t_user u, (select @val \:= 0) t where deleted = 0"""
        val res1 = bef.replace(regex, replacement)
        Assertions.assertEquals(res, res1)
        val res2 = res.replace(regex, replacement)
        Assertions.assertEquals(res, res2)
    }
}
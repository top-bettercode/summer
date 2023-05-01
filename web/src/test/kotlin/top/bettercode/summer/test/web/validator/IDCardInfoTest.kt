package top.bettercode.summer.test.web.validator

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import top.bettercode.summer.web.validator.IDCardInfo

/**
 * @author Peter Wu
 * @since 0.1.15
 */
class IDCardInfoTest {
    @Test
    fun info() {
        Assertions.assertEquals("出生地：四川省宜宾市,生日：1987年2月22日,性别：男",
                IDCardInfo("511521198702223935").toString())
    }
}
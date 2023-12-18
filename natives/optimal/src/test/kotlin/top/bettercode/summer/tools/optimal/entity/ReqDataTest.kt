package top.bettercode.summer.tools.optimal.entity

import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.util.StringUtil.json

/**
 * @author Peter Wu
 */
internal class ReqDataTest {

    @Test
    fun testReadData() {
//        val data = ReqData("13-05-07高氯枸磷")
//        val data = ReqData("24-06-10高氯枸磷")
//        val data = ReqData("15-15-15喷浆氯基")
//        val data = ReqData("15-15-15喷浆硫基")
        val data = ReqData("15-15-15常规氯基")
        System.err.println(json(data, true))
    }

    // 检查价格
    @Test
    fun checkPrice() {
        var data = ReqData("13-05-07高氯枸磷")
        data.checkPrice()
        data = ReqData("24-06-10高氯枸磷")
        data.checkPrice()
        data = ReqData("15-15-15喷浆氯基")
        data.checkPrice()
        data = ReqData("15-15-15喷浆硫基")
        data.checkPrice()
        data = ReqData("15-15-15常规氯基")
        data.checkPrice()
    }
}

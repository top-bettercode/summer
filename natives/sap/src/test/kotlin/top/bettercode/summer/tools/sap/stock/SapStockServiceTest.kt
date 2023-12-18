package top.bettercode.summer.tools.sap.stock

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.tools.sap.TestApplication

/**
 * @author Peter Wu
 */
@SpringBootTest(classes = [TestApplication::class])
@Disabled
internal class SapStockServiceTest {
    @Autowired
    var orderToSap: SapStockService? = null

    @Test
    @Disabled
    fun testGetStore() {
        val store = orderToSap!!.getStore("20103962", "1090")
        //    StockLsReturn store = orderToSap.getStore("20101131", "1330");
        System.err.println(json(store, true))
    }

    @Test
    fun testGetStores() {
        var s = System.currentTimeMillis()
        val store1 = orderToSap!!.getStore("20103962", "1090")
        val store2 = orderToSap!!.getStore("20103962", "1090")
        val store3 = orderToSap!!.getStore("20101131", "1330")
        var e = System.currentTimeMillis()
        System.err.println(e - s)
        System.err.println(json(store1, true))
        System.err.println(json(store2, true))
        System.err.println(json(store3, true))
        s = System.currentTimeMillis()
        val stores = orderToSap!!.getStores(
                setOf(StockQuery("1090", "20103962"),
                        StockQuery("1090", "20103962"),
                        StockQuery("1330", "20101131")))
        e = System.currentTimeMillis()
        System.err.println(e - s)
        System.err.println(json(stores, true))
    }
}
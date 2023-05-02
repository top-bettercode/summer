package top.bettercode.summer.tools.sap.stock

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import top.bettercode.summer.tools.lang.util.StringUtil.valueOf
import top.bettercode.summer.tools.sap.TestApplication

/**
 * @author Peter Wu
 */
@SpringBootTest(classes = [TestApplication::class])
internal class SapStockServiceTest {
    @Autowired
    var orderToSap: SapStockService? = null

    @get:Test
    @get:Disabled
    val store: Unit
        get() {
            val store = orderToSap!!.getStore("20103962", "1090")
            //    StockLsReturn store = orderToSap.getStore("20101131", "1330");
            System.err.println(valueOf(store, true))
        }

    @get:Test
    val stores: Unit
        get() {
            var s = System.currentTimeMillis()
            val store1 = orderToSap!!.getStore("20103962", "1090")
            val store2 = orderToSap!!.getStore("20103962", "1090")
            val store3 = orderToSap!!.getStore("20101131", "1330")
            var e = System.currentTimeMillis()
            System.err.println(e - s)
            System.err.println(valueOf(store1, true))
            System.err.println(valueOf(store2, true))
            System.err.println(valueOf(store3, true))
            s = System.currentTimeMillis()
            val stores = orderToSap!!.getStores(
                    setOf(StockQuery("1090", "20103962"),
                            StockQuery("1090", "20103962"),
                            StockQuery("1330", "20101131")))
            e = System.currentTimeMillis()
            System.err.println(e - s)
            System.err.println(valueOf(stores, true))
        }
}
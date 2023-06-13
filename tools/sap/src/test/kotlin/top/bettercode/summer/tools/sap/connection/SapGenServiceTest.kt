package top.bettercode.summer.tools.sap.connection

import com.sap.conn.jco.JCoException
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import top.bettercode.summer.tools.sap.TestApplication
import java.io.IOException

/**
 * @author Peter Wu
 */
@SpringBootTest(classes = [TestApplication::class])
internal class SapGenServiceTest {
    @Autowired
    var sapGenService: SapGenService? = null
    @Test
    fun gen() {
//    sapGenService.gen("STOCK", "ZRFC_STOCK_001");
        sapGenService!!.gen("COST", "ZRFC_OA_COST")
        //    sapGenService.gen("COST", "ZRFC_ORD_CONFIRM");

        //ZRFC_DN_MODIFY
        //ZRFC_ORD_CONFIRM
        //ZRFC_DN_DEL
        //ZRFC_DN_CREATE
    }
}
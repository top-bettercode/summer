package top.bettercode.summer.tools.sap.connection;

import com.sap.conn.jco.JCoException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.bettercode.summer.tools.sap.TestApplication;

/**
 * @author Peter Wu
 */
@SpringBootTest(classes = TestApplication.class)
class SapGenServiceTest {

  @Autowired
  SapGenService sapGenService;

  @Test
  void gen() throws JCoException, IOException {
//    sapGenService.gen("STOCK", "ZRFC_STOCK_001");
    sapGenService.gen("COST", "ZRFC_OA_COST");
//    sapGenService.gen("COST", "ZRFC_ORD_CONFIRM");

    //ZRFC_DN_MODIFY
    //ZRFC_ORD_CONFIRM
    //ZRFC_DN_DEL
    //ZRFC_DN_CREATE
  }
}
package top.bettercode.summer.tools.sap.stock;

import java.util.Map;
import kotlin.collections.SetsKt;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.bettercode.summer.tools.lang.util.StringUtil;
import top.bettercode.summer.tools.sap.TestApplication;

/**
 * @author Peter Wu
 */
@SpringBootTest(classes = TestApplication.class)
class SapStockServiceTest {

  @Autowired
  SapStockService orderToSap;

  @Disabled
  @Test
  public void getStore() {
    StockLsReturn store = orderToSap.getStore("20103962", "1090");
//    StockLsReturn store = orderToSap.getStore("20101131", "1330");
    System.err.println(StringUtil.valueOf(store, true));
  }

  @Test
  public void getStores() {
    long s = System.currentTimeMillis();
    StockLsReturn store1 = orderToSap.getStore("20103962", "1090");
    StockLsReturn store2 = orderToSap.getStore("20103962", "1090");
    StockLsReturn store3 = orderToSap.getStore("20101131", "1330");
    long e = System.currentTimeMillis();
    System.err.println(e - s);
    System.err.println(StringUtil.valueOf(store1, true));
    System.err.println(StringUtil.valueOf(store2, true));
    System.err.println(StringUtil.valueOf(store3, true));
    s = System.currentTimeMillis();
    Map<StockQuery, StockLsReturn> stores = orderToSap.getStores(
        SetsKt.setOf(new StockQuery("1090", "20103962"),
            new StockQuery("1090", "20103962"),
            new StockQuery("1330", "20101131")));
    e = System.currentTimeMillis();
    System.err.println(e - s);
    System.err.println(StringUtil.valueOf(stores, true));
  }

}
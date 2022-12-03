package top.bettercode.summer.tools.sap.cost;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.bettercode.summer.tools.sap.TestApplication;

/**
 * @author Peter Wu
 */
@SpringBootTest(classes = TestApplication.class)
class SapCostServiceTest {

  @Autowired
  SapCostService sapCostService;

  @Disabled
  @Test
  void getCosts() {
    CostResp costs = sapCostService.getCosts();
    System.err.println(costs);
  }
}
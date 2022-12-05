package top.bettercode.summer.tools.sap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import top.bettercode.summer.tools.sap.connection.SapService;
import top.bettercode.summer.tools.sap.cost.SapCostService;
import top.bettercode.summer.tools.sap.stock.SapStockService;

/**
 * @author Peter Wu
 */
@SpringBootApplication
public class TestApplication {

  public static void main(String[] args) {
    SpringApplication.run(TestApplication.class, args);
  }

  @Bean
  public SapCostService sapCostService(SapService sapService) {
    return new SapCostService(sapService);
  }

  @Bean
  public SapStockService sapStockService(SapService sapService) {
    return new SapStockService(sapService);
  }
}

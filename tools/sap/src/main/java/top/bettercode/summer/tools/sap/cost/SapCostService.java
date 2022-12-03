package top.bettercode.summer.tools.sap.cost;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.bettercode.summer.tools.sap.connection.SapService;

public class SapCostService {

  private final Logger log = LoggerFactory.getLogger(SapCostService.class);
  private final SapService sapService;

  public SapCostService(SapService sapService) {
    this.sapService = sapService;
  }

  public CostResp getCosts() {
    return sapService.invoke("ZRFC_OA_COST", null, CostResp.class);
  }

}

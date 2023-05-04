package top.bettercode.summer.tools.sap.cost

import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.sap.connection.SapService

class SapCostService(private val sapService: SapService) {
    private val log = LoggerFactory.getLogger(SapCostService::class.java)
    val costs: CostResp
        get() = sapService.invoke("ZRFC_OA_COST", null, CostResp::class.java)
}

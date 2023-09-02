package top.bettercode.summer.tools.sap.cost

import top.bettercode.summer.tools.sap.connection.SapService

class SapCostService(private val sapService: SapService) {
    val costs: CostResp
        get() = sapService.invoke("ZRFC_OA_COST", null, CostResp::class.java)
}

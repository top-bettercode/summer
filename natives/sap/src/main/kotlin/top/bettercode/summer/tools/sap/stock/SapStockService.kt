package top.bettercode.summer.tools.sap.stock

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.lang.client.ClientSysException
import top.bettercode.summer.tools.sap.connection.SapService
import top.bettercode.summer.tools.sap.connection.pojo.SapHead
import java.math.BigDecimal

class SapStockService(private val sapService: SapService) {

    private val log = LoggerFactory.getLogger(SapStockService::class.java)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun getStore(materialId: String?, factoryId: String?): StockLsReturn? {
        return try {
            val sapQueryStock = StockReq()
            sapQueryStock.iPlant = factoryId //工厂
            sapQueryStock.iMaterial = materialId //物料编码
            val head = SapHead()
            head.ifid = "STOCK_QUERY_IN"
            head.sysid = "CRM2"
            sapQueryStock.head = head
            val stockResp =
                sapService.invoke("ZRFC_STOCK_001", sapQueryStock, StockResp::class.java)
            stockResp.lsReturn
        } catch (e: ClientSysException) {
            val lsReturn = StockLsReturn()
            lsReturn.meins = "TO"
            lsReturn
        } catch (e: Exception) {
            log.warn(e.message, e)
            val lsReturn = StockLsReturn()
            lsReturn.menge1 = BigDecimal(-1)
            lsReturn.menge2 = BigDecimal(-1)
            lsReturn.meins = "TO"
            lsReturn
        }
    }

    fun getStores(params: Set<StockQuery>): Map<StockQuery, StockLsReturn?> {
        return runBlocking {
            params.map {
                scope.async {
                    it to getStore(it.materialId, it.factoryId)
                }
            }.awaitAll().toMap()
        }
    }
}

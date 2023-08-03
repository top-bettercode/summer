package top.bettercode.summer.tools.sap.stock

import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.sap.connection.SapService
import top.bettercode.summer.tools.sap.connection.SapSysException
import top.bettercode.summer.tools.sap.connection.pojo.SapHead
import java.math.BigDecimal
import java.util.concurrent.*

class SapStockService {
    private val log = LoggerFactory.getLogger(SapStockService::class.java)
    private val sapService: SapService
    private val executor: ThreadPoolExecutor

    constructor(sapService: SapService) {
        this.sapService = sapService
        executor = ThreadPoolExecutor(10, 100, 0L, TimeUnit.MILLISECONDS,
                LinkedBlockingQueue(16), ThreadPoolExecutor.CallerRunsPolicy())
    }

    constructor(sapService: SapService, executor: ThreadPoolExecutor) {
        this.sapService = sapService
        this.executor = executor
    }

    fun getStore(materialId: String?, factoryId: String?): StockLsReturn? {
        return try {
            val sapQueryStock = StockReq()
            sapQueryStock.setIPlant(factoryId) //工厂
            sapQueryStock.setIMaterial(materialId) //物料编码
            val head = SapHead()
            head.setIfid("STOCK_QUERY_IN")
            head.setSysid("CRM2")
            sapQueryStock.setHead(head)
            val stockResp = sapService.invoke("ZRFC_STOCK_001", sapQueryStock, StockResp::class.java)
            stockResp.lsReturn
        } catch (e: SapSysException) {
            val lsReturn = StockLsReturn()
            lsReturn.setMeins("TO")
            lsReturn
        } catch (e: Exception) {
            log.warn(e.message, e)
            val lsReturn = StockLsReturn()
            lsReturn.setMenge1(BigDecimal(-1))
            lsReturn.setMenge2(BigDecimal(-1))
            lsReturn.setMeins("TO")
            lsReturn
        }
    }

    fun getStores(params: Set<StockQuery>): Map<StockQuery, StockLsReturn> {
        return try {
            val countDownLatch = CountDownLatch(params.size)
            val futures: MutableMap<StockQuery, Future<StockLsReturn>> = ConcurrentHashMap()
            for (param in params) {
                val future = executor.submit<StockLsReturn> {
                    try {
                        return@submit getStore(param.materialId, param.factoryId)
                    } finally {
                        countDownLatch.countDown()
                    }
                }
                futures[param] = future
            }
            countDownLatch.await()
            val results: MutableMap<StockQuery, StockLsReturn> = ConcurrentHashMap()
            for ((key, value) in futures) {
                try {
                    results[key] = value.get()
                } catch (e: ExecutionException) {
                    throw RuntimeException(e)
                }
            }
            results
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }
}

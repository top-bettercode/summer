package top.bettercode.summer.tools.sap

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import top.bettercode.summer.tools.sap.connection.SapService
import top.bettercode.summer.tools.sap.cost.SapCostService
import top.bettercode.summer.tools.sap.stock.SapStockService

/**
 * @author Peter Wu
 */
@SpringBootApplication
class TestApplication {
    @Bean
    fun sapCostService(sapService: SapService): SapCostService {
        return SapCostService(sapService)
    }

    @Bean
    fun sapStockService(sapService: SapService): SapStockService {
        return SapStockService(sapService)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(TestApplication::class.java, *args)
        }
    }
}

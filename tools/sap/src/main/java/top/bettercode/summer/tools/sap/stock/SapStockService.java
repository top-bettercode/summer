package top.bettercode.summer.tools.sap.stock;


import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.bettercode.summer.tools.sap.connection.SapService;
import top.bettercode.summer.tools.sap.connection.SapSysException;
import top.bettercode.summer.tools.sap.connection.pojo.SapHead;

public class SapStockService {

  private final Logger log = LoggerFactory.getLogger(SapStockService.class);
  private final SapService sapService;
  private final ThreadPoolExecutor executor;

  public SapStockService(SapService sapService) {
    this.sapService = sapService;
    executor = new ThreadPoolExecutor(10, 100, 0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<>(16), new CallerRunsPolicy());
  }

  public SapStockService(SapService sapService, ThreadPoolExecutor executor) {
    this.sapService = sapService;
    this.executor = executor;
  }

  public StockLsReturn getStore(String materialId, String factoryId) {
    try {
      StockReq sapQueryStock = new StockReq();
      sapQueryStock.setIPlant(factoryId);//工厂
      sapQueryStock.setIMaterial(materialId);//物料编码
      SapHead head = new SapHead();
      head.setIfid("STOCK_QUERY_IN");
      head.setSysid("CRM2");
      sapQueryStock.setHead(head);
      StockResp stockResp = sapService.invoke("ZRFC_STOCK_001", sapQueryStock, StockResp.class);
      return stockResp.getLsReturn();
    } catch (SapSysException e) {
      StockLsReturn lsReturn = new StockLsReturn();
      lsReturn.setMeins("TO");
      return lsReturn;
    } catch (Exception e) {
      log.warn(e.getMessage(), e);
      StockLsReturn lsReturn = new StockLsReturn();
      lsReturn.setMenge1(new BigDecimal(-1));
      lsReturn.setMenge2(new BigDecimal(-1));
      lsReturn.setMeins("TO");
      return lsReturn;
    }
  }

  public Map<StockQuery, StockLsReturn> getStores(Set<StockQuery> params) {
    try {
      CountDownLatch countDownLatch = new CountDownLatch(params.size());
      Map<StockQuery, Future<StockLsReturn>> futures = new ConcurrentHashMap<>();
      for (StockQuery param : params) {
        Future<StockLsReturn> future = executor.submit(() -> {
          try {
            return getStore(param.getMaterialId(), param.getFactoryId());
          } finally {
            countDownLatch.countDown();
          }
        });
        futures.put(param, future);
      }
      countDownLatch.await();

      Map<StockQuery, StockLsReturn> results = new ConcurrentHashMap<>();
      for (Entry<StockQuery, Future<StockLsReturn>> entry : futures.entrySet()) {
        try {
          results.put(entry.getKey(), entry.getValue().get());
        } catch (ExecutionException e) {
          throw new RuntimeException(e);
        }
      }
      return results;
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

}

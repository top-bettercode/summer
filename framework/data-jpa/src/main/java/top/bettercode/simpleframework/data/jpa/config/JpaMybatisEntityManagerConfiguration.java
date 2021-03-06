package top.bettercode.simpleframework.data.jpa.config;

import java.lang.reflect.Field;
import java.util.List;
import javax.persistence.EntityManager;
import org.hibernate.engine.query.spi.QueryPlanCache;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.context.annotation.Configuration;
import top.bettercode.simpleframework.data.jpa.query.mybatis.MybatisQueryInterpreterStandardImpl;

/**
 * DataJpaConfiguration 配置
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
public class JpaMybatisEntityManagerConfiguration {

  @SuppressWarnings("deprecation")
  public JpaMybatisEntityManagerConfiguration(List<EntityManager> entityManagers) {
    entityManagers.forEach(entityManager -> {
      SessionFactoryImplementor factoryImplementor = entityManager.getEntityManagerFactory()
          .unwrap(SessionFactoryImplementor.class);
      QueryPlanCache queryPlanCache = factoryImplementor.getQueryPlanCache();
      try {
        Field field = QueryPlanCache.class.getDeclaredField("nativeQueryInterpreter");
        field.setAccessible(true);
        field.set(queryPlanCache, new MybatisQueryInterpreterStandardImpl(factoryImplementor));
      } catch (NoSuchFieldException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    });
  }
}

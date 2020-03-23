package cn.bestwu.simpleframework.data;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * 数据仓库信息
 *
 * @author Peter Wu
 */
public class Repositories implements Iterable<Class<?>> {

  private final Logger log = LoggerFactory.getLogger(Repositories.class);
  private final Map<Class<?>, RepositoryMetadata> repositoryMetadatas;

  public Repositories(List<MapperFactoryBean> mapperFactoryBeans,
      Set<Class<?>> allSubClasses) {
    this.repositoryMetadatas = new HashMap<>();

    if (!CollectionUtils.isEmpty(mapperFactoryBeans)) {
      populateRepositoryMetadata(mapperFactoryBeans, allSubClasses);
    }
  }

  @SuppressWarnings("unchecked")
  private void populateRepositoryMetadata(List<MapperFactoryBean> mapperFactoryBeans,
      Set<Class<?>> allSubClasses) {
    Map<Class, Class> queryDsls = new HashMap<>();
    for (Class<?> queryDslType : allSubClasses) {
      Class<?> modelClass = (Class) ((ParameterizedType) queryDslType.getGenericSuperclass())
          .getActualTypeArguments()[1];
      if (log.isDebugEnabled()) {
        log.debug("Detected queryDsl:{}=>{}", modelClass, queryDslType);
      }
      queryDsls.put(modelClass, queryDslType);
    }
    try {
      for (MapperFactoryBean mapperFactoryBean : mapperFactoryBeans) {
        if (BaseMapper.class.isAssignableFrom(mapperFactoryBean.getMapperInterface())) {
          RepositoryMetadata repositoryMetadata = new RepositoryMetadata(
              (BaseMapper) mapperFactoryBean.getObject(), mapperFactoryBean.getMapperInterface(),
              queryDsls);
          putRepositoryMetadata(repositoryMetadata);
        }
      }
    } catch (Exception e) {
      log.error("populateRepositoryMetadata fail", e);
    }
  }

  public void putRepositoryMetadata(RepositoryMetadata repositoryMetadata) {
    repositoryMetadatas.put(repositoryMetadata.getModelClass(), repositoryMetadata);
  }

  public boolean hasRepositoryFor(Class<?> domainClass) {

    Assert.notNull(domainClass, "Domain type must not be null!");

    return repositoryMetadatas.containsKey(domainClass);
  }

  public RepositoryMetadata getRepositoryMetadataFor(Class<?> domainClass) {
    Assert.notNull(domainClass, "Domain type must not be null!");
    return repositoryMetadatas.get(domainClass);
  }

  @Override
  public Iterator<Class<?>> iterator() {
    return repositoryMetadatas.keySet().iterator();
  }
}
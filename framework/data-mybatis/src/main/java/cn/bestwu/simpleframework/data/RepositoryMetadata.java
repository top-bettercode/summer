package cn.bestwu.simpleframework.data;

import cn.bestwu.simpleframework.data.binding.ConditionWrapper;
import cn.bestwu.simpleframework.data.dsl.EntityPathWrapper;
import com.baomidou.mybatisplus.entity.TableFieldInfo;
import com.baomidou.mybatisplus.entity.TableInfo;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.toolkit.TableInfoHelper;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.Assert;

/**
 * 资源元数据
 *
 * @author Peter Wu
 */
@SuppressWarnings("unchecked")
public class RepositoryMetadata {

  private final Object repository;
  private final Map<String, String> cachedFieldsMap;
  private Class<?> modelClass;
  private Class<? extends Serializable> idType;
  private final Class<? extends EntityPathWrapper<?, ?>> queryDslType;

  //  	private Method findAllMethod;
  private Method wrapperBinderMethod;
  private final Method findOneMethod;
  //	private Method saveMethod;
  //	private Method deleteMethod;
  //--------------------------------------------

  /**
   * 创建资源元数据
   *
   * @param repository 仓库
   * @param repositoryInterface repositoryInterface
   * @param queryDsls queryDsls
   * @throws NoSuchMethodException NoSuchMethodException
   * @throws NoSuchFieldException NoSuchFieldException
   */
  public RepositoryMetadata(BaseMapper repository,
      Class<? extends BaseMapper> repositoryInterface,
      Map<Class, Class> queryDsls) throws NoSuchFieldException, NoSuchMethodException {
    this.repository = repository;

    init(repositoryInterface);
    Assert.notNull(modelClass, "未成功设置 modelClass，repository:" + repositoryInterface.getName()
        + " 未继承 " + BaseMapper.class.getTypeName());
    queryDslType = queryDsls.get(modelClass);
    if (queryDslType != null) {
      try {
        wrapperBinderMethod = repositoryInterface.getMethod("customize", queryDslType);
      } catch (NoSuchMethodException ignored) {
      }
    }
    findOneMethod = repositoryInterface.getMethod("selectById", Serializable.class);
    Map<String, String> cachedFieldsMap = new HashMap<>();
    TableInfo tableInfo = TableInfoHelper.getTableInfo(modelClass);

    for (TableFieldInfo tableFieldInfo : tableInfo.getFieldList()) {
      cachedFieldsMap.put(tableFieldInfo.getProperty(), tableFieldInfo.getColumn());
    }
    String keyProperty = tableInfo.getKeyProperty();
    if (keyProperty != null) {
      cachedFieldsMap.put(keyProperty, tableInfo.getKeyColumn());
      this.idType = (Class<? extends Serializable>) modelClass.getDeclaredField(keyProperty)
          .getType();
    }
    this.cachedFieldsMap = Collections.unmodifiableMap(cachedFieldsMap);
  }

  private void init(Class<?> repositoryInterface) {
    Type[] genericInterfaces = repositoryInterface.getGenericInterfaces();

    for (Type genericInterface : genericInterfaces) {
      if (genericInterface instanceof ParameterizedType && ((ParameterizedType) genericInterface)
          .getRawType().getTypeName().equals(BaseMapper.class.getName())) {
        Type[] typeArguments = ((ParameterizedType) genericInterface)
            .getActualTypeArguments();
        modelClass = (Class) typeArguments[0];
      } else if (genericInterface instanceof Class
          && ((Class) genericInterface).getGenericInterfaces().length > 0) {
        init((Class<?>) genericInterface);
      }
    }
  }

  //--------------------------------------------

  public Object invokeFindOne(Serializable id)
      throws InvocationTargetException, IllegalAccessException {
    return findOneMethod.invoke(repository, id);
  }

  public void invokeCustomize(ConditionWrapper<?> entityPathWrapper)
      throws InvocationTargetException, IllegalAccessException {
    if (wrapperBinderMethod != null) {
      wrapperBinderMethod.invoke(repository, entityPathWrapper);
    }
  }

  //--------------------------------------------

  public Class<?> getModelClass() {
    return modelClass;
  }

  public Class<? extends Serializable> getIdType() {
    return idType;
  }

  public Method getWrapperBinderMethod() {
    return wrapperBinderMethod;
  }

  public Method getFindOneMethod() {
    return findOneMethod;
  }

  public Object getRepository() {
    return repository;
  }

  public Map<String, String> getCachedFieldsMap() {
    return cachedFieldsMap;
  }

  public Class<? extends EntityPathWrapper<?, ?>> getQueryDslType() {
    return queryDslType;
  }

  public EntityPathWrapper getWrapper(Class<EntityPathWrapper> parameterType)
      throws IllegalAccessException, InstantiationException {
    return queryDslType == null ? parameterType.newInstance() : queryDslType.newInstance();
  }
}

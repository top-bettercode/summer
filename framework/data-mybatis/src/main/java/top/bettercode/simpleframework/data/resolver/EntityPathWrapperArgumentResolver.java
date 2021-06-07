package top.bettercode.simpleframework.data.resolver;

import top.bettercode.simpleframework.data.Repositories;
import top.bettercode.simpleframework.data.RepositoryMetadata;
import top.bettercode.simpleframework.data.binding.WrapperBinderProperties;
import top.bettercode.simpleframework.data.dsl.EntityPathWrapper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Wrapper参数解析
 *
 * @author Peter Wu
 */
public class EntityPathWrapperArgumentResolver implements HandlerMethodArgumentResolver {

  private final Repositories repositories;
  private final WrapperBinderProperties properties;

  public EntityPathWrapperArgumentResolver(
      Repositories repositories,
      WrapperBinderProperties properties) {
    this.repositories = repositories;
    this.properties = properties;
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType().isAssignableFrom(EntityPathWrapper.class)
        || EntityPathWrapper.class.equals(parameter.getParameterType().getSuperclass());
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
    Class modelClass;
    Class<?> parameterType = parameter.getParameterType();
    if (EntityPathWrapper.class.equals(parameterType.getSuperclass())) {
      modelClass = (Class) ((ParameterizedType) parameterType.getGenericSuperclass())
          .getActualTypeArguments()[1];
    } else {
      modelClass = (Class) ((ParameterizedType) parameter.getGenericParameterType())
          .getActualTypeArguments()[0];
    }
    return resolveWrapper(webRequest, modelClass, parameterType);
  }

  @SuppressWarnings("unchecked")
  private Object resolveWrapper(NativeWebRequest request, Class modelClass,
      Class<?> parameterType)
      throws InvocationTargetException, IllegalAccessException, InstantiationException {
    String orderBy = request.getParameter(properties.getOrderByParameter());
    String isAsc = request.getParameter(properties.getIsAscParameter());
    if (!StringUtils.hasText(isAsc)) {
      isAsc = properties.getDefaultIsAsc();
    }
    String groupBy = request.getParameter(properties.getGroupByParameter());
    String isNull = request.getParameter(properties.getIsNullParameter());
    String isNotNull = request.getParameter(properties.getIsNotNullParameter());
    RepositoryMetadata repositoryMetadataFor = repositories.getRepositoryMetadataFor(modelClass);
    EntityPathWrapper wrapper = repositoryMetadataFor.getWrapper(
        (Class<EntityPathWrapper>) parameterType);
    Map<String, String> cachedFieldsMap = repositoryMetadataFor.getCachedFieldsMap();
    if (StringUtils.hasText(orderBy)) {
      for (String s : orderBy.split(",")) {
        if (StringUtils.hasText(s)) {
          String property = s.split(" ")[0];
          if (StringUtils.hasText(property)) {
            String column = cachedFieldsMap.get(property.trim());
            if (column != null) {
              orderBy = orderBy.replace(property, column);
            }
          }
        }
      }
    }
    groupBy = convertProperty2Column(groupBy, cachedFieldsMap);
    isNull = convertProperty2Column(isNull, cachedFieldsMap);
    isNotNull = convertProperty2Column(isNotNull, cachedFieldsMap);

    if (StringUtils.hasText(orderBy)) {
      wrapper.orderBy(orderBy, isAsc.equalsIgnoreCase("asc"));
      wrapper.setSetOrderBy(true);
    }
    if (StringUtils.hasText(groupBy)) {
      wrapper.groupBy(groupBy);
      wrapper.setSetGroupBy(true);
    }
    if (StringUtils.hasText(isNull)) {
      wrapper.isNull(isNull);
      wrapper.setSetIsNull(true);
    }
    if (StringUtils.hasText(isNotNull)) {
      wrapper.isNotNull(isNotNull);
      wrapper.setSetIsNotNull(true);
    }

    Set<String> keySet = new HashSet<>(request.getParameterMap().keySet());
    keySet.remove(properties.getOrderByParameter());
    keySet.remove(properties.getGroupByParameter());
    keySet.remove(properties.getIsAscParameter());
    keySet.remove(properties.getIsNullParameter());
    keySet.remove(properties.getIsNotNullParameter());
    for (String property : keySet) {
      String column = cachedFieldsMap.get(property);
      if (column != null) {
        String value = request.getParameter(property);
        if (StringUtils.hasText(value)) {
          wrapper.addConditions(column, value);
        }
      } else {
        String value = request.getParameter(property);
        if (StringUtils.hasText(value)) {
          wrapper.addAdditions(property, value);
        }
      }
    }

    if (repositoryMetadataFor.getWrapperBinderMethod() != null) {
      repositoryMetadataFor.invokeCustomize(wrapper);
    }
    wrapper.doDefault();
    return wrapper;
  }

  private String convertProperty2Column(String properties, Map<String, String> cachedFieldsMap) {
    if (StringUtils.hasText(properties)) {
      for (String s : properties.split(",")) {
        if (StringUtils.hasText(s)) {
          String column = cachedFieldsMap.get(s.trim());
          if (column != null) {
            properties = properties.replace(s, column);
          }
        }
      }
    }
    return properties;
  }

}

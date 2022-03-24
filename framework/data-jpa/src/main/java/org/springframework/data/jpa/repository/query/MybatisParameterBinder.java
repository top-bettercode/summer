package org.springframework.data.jpa.repository.query;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.Query;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.data.jpa.repository.query.JpaParameters.JpaParameter;
import org.springframework.data.jpa.repository.query.QueryParameterSetter.BindableQuery;
import org.springframework.data.jpa.repository.query.QueryParameterSetter.ErrorHandling;
import org.springframework.data.jpa.repository.query.QueryParameterSetter.QueryMetadata;
import top.bettercode.simpleframework.data.jpa.query.mybatis.MybatisParam;
import top.bettercode.simpleframework.data.jpa.support.Size;

/**
 * @author Peter Wu
 */
public class MybatisParameterBinder extends ParameterBinder {

  private static final String GENERIC_NAME_PREFIX = "param";

  private final JpaParameters parameters;
  private final TypeHandlerRegistry typeHandlerRegistry;
  private final MappedStatement mappedStatement;
  private final Configuration configuration;

  public MybatisParameterBinder(JpaParameters parameters,
      MappedStatement mappedStatement) {
    super(parameters, Collections.emptyList());
    this.parameters = parameters;
    this.mappedStatement = mappedStatement;
    this.configuration = mappedStatement.getConfiguration();
    this.typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
  }

  @Deprecated
  @Override
  public <T extends Query> T bind(T jpaQuery, QueryMetadata metadata,
      JpaParametersParameterAccessor accessor) {
    return jpaQuery;
  }

  @Deprecated
  @Override
  public void bind(BindableQuery query, JpaParametersParameterAccessor accessor,
      ErrorHandling errorHandling) {
  }


  @Deprecated
  @Override
  Query bindAndPrepare(Query query, QueryMetadata metadata,
      JpaParametersParameterAccessor accessor) {
    return query;
  }

  void bind(BindableQuery query, MybatisParam mybatisParam) {
    ErrorContext.instance().activity("setting parameters")
        .object(mappedStatement.getParameterMap().getId());
    ErrorHandling errorHandling = ErrorHandling.STRICT;

    BoundSql boundSql = mybatisParam.getBoundSql();
    Object parameterObject = mybatisParam.getParameterObject();
    List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    if (parameterMappings != null) {
      for (int i = 0; i < parameterMappings.size(); i++) {
        ParameterMapping parameterMapping = parameterMappings.get(i);
        if (parameterMapping.getMode() != ParameterMode.OUT) {
          Object value;
          String propertyName = parameterMapping.getProperty();
          if (boundSql.hasAdditionalParameter(propertyName)) {
            value = boundSql.getAdditionalParameter(propertyName);
          } else if (parameterObject == null) {
            value = null;
          } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
            value = parameterObject;
          } else {
            MetaObject metaObject = configuration.newMetaObject(parameterObject);
            value = metaObject.getValue(propertyName);
          }
          try {
            int position = i + 1;
            errorHandling.execute(() -> query.setParameter(position, value));
          } catch (Exception e) {
            throw new TypeException(
                "Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);
          }
        }
      }
    }

  }


  Query bindAndPrepare(Query query,QueryParameterSetter.QueryMetadata metadata, JpaParametersParameterAccessor accessor,
      MybatisParam mybatisParam) {

    bind(metadata.withQuery(query), mybatisParam);

    Size size = mybatisParam.getSize();

    if (size != null) {
      query.setFirstResult(0);
      query.setMaxResults(size.getSize());
    }

    if (!parameters.hasPageableParameter() || accessor.getPageable().isUnpaged()) {
      return query;
    }

    query.setFirstResult((int) accessor.getPageable().getOffset());
    query.setMaxResults(accessor.getPageable().getPageSize());

    return query;
  }


  public MybatisParam bindParameterObject(JpaParametersParameterAccessor accessor) {
    Size size = null;
    Object[] values = accessor.getValues();
    int bindableSize = 0;
    final Map<String, Object> paramMap = new ParamMap<>();
    Set<String> names = this.parameters.stream().map(p -> p.getName().orElse(null))
        .filter(Objects::nonNull).collect(Collectors.toSet());
    Object params = null;
    for (JpaParameter parameter : this.parameters.getBindableParameters()) {
      Class<?> parameterType = parameter.getType();
      int parameterIndex = parameter.getIndex();
      Object value = values[parameterIndex];
      if (Size.class.isAssignableFrom(parameterType)) {
        size = (Size) value;
      } else {
        Optional<String> name = parameter.getName();
        name.ifPresent(s -> paramMap.put(s, value));

        String otherName = GENERIC_NAME_PREFIX + (bindableSize + 1);
        if (!names.contains(otherName)) {
          paramMap.put(otherName, value);
        }

        if (bindableSize == 0) {
          params = ParamNameResolver.wrapToMapIfCollection(value, name.orElse(null));
        }

        bindableSize++;
      }
    }
    params = bindableSize > 1 ? paramMap : params;
    Object parameterObject = params;
    BoundSql boundSql = mappedStatement.getBoundSql(parameterObject);
    return new MybatisParam(boundSql, parameterObject, size);
  }

}

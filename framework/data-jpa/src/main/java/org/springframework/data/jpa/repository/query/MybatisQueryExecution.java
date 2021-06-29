package org.springframework.data.jpa.repository.query;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.JpaParameters.JpaParameter;
import org.springframework.data.repository.core.support.SurroundingTransactionDetectorMethodInterceptor;
import org.springframework.util.StringUtils;

public abstract class MybatisQueryExecution extends JpaQueryExecution {

  private static final String GENERIC_NAME_PREFIX = "param";

  @Override
  protected Object doExecute(AbstractJpaQuery query, JpaParametersParameterAccessor accessor) {
    return doMybatisExecute((MybatisQuery) query, accessor);
  }

  protected abstract Object doMybatisExecute(MybatisQuery mybatisQuery,
      JpaParametersParameterAccessor accessor);

  static class CollectionExecution extends MybatisQueryExecution {

    @Override
    protected Object doMybatisExecute(MybatisQuery query, JpaParametersParameterAccessor accessor) {
      Object[] values = accessor.getValues();
      String statement = query.getQueryMethod().getStatement();
      if (null == values || values.length == 0) {
        return query.getSqlSessionTemplate().selectList(statement);
      }
      JpaParameters parameters = query.getQueryMethod().getParameters();
      if (parameters.hasPageableParameter()) {
        throw new IllegalArgumentException(
            "当包含org.springframework.data.domain.Pageable参数时返回类型必须为org.springframework.data.domain.Page");
      }

      Object params = getMybatisParameters(parameters, values);

      String sort = MybatisQueryExecution.getSort(parameters, values);
      if (StringUtils.hasText(sort)) {
        PageHelper.orderBy(sort);
        Page<Object> localPage = PageHelper.getLocalPage();
        localPage.setCount(false);
        return localPage
            .doSelectPage(() -> query.getSqlSessionTemplate().selectList(statement, params))
            .getResult();
      } else {
        return query.getSqlSessionTemplate().selectList(statement, params);
      }
    }

  }

  static class PagedExecution extends MybatisQueryExecution {

    @Override
    protected Object doMybatisExecute(MybatisQuery query, JpaParametersParameterAccessor accessor) {
      Object[] values = accessor.getValues();
      String statement = query.getQueryMethod().getStatement();

      JpaParameters parameters = query.getQueryMethod().getParameters();
      Object params = getMybatisParameters(parameters, values);

      Pageable pageable = null;
      if (parameters.hasPageableParameter()) {
        pageable = (Pageable) values[parameters.getPageableIndex()];
      }
      String sort = MybatisQueryExecution.getSort(parameters, values);
      if (StringUtils.hasText(sort)) {
        PageHelper.orderBy(sort);
      }
      List<Object> result;
      ISelect iSelect = () -> query.getSqlSessionTemplate().selectList(statement, params);
      if (null == pageable || pageable == Pageable.unpaged()) {
        if (StringUtils.hasText(sort)) {
          Page<Object> page = PageHelper.getLocalPage().doSelectPage(iSelect);
          result = page.getResult();
        } else {
          result = query.getSqlSessionTemplate().selectList(statement, params);
        }
        return new PageImpl<>(result, pageable, null == result ? 0 : result.size());
      }

      int pageSize = pageable.getPageSize();
      int pageNumber = pageable.getPageNumber();

      Page<Object> page = PageHelper.startPage(pageNumber + 1, pageSize).doSelectPage(iSelect);

      return new PageImpl<>(page.getResult(), pageable, page.getTotal());
    }

  }

  static class SlicedExecution extends PagedExecution {

    @SuppressWarnings("unchecked")
    @Override
    protected Object doMybatisExecute(MybatisQuery query, JpaParametersParameterAccessor accessor) {
      PageImpl<Object> page = (PageImpl<Object>) super.doMybatisExecute(query, accessor);

      Pageable pageable = page.getPageable();
      return new SliceImpl<>(page.getContent(), pageable,
          page.getTotalElements() > (long) (pageable.getPageNumber() + 1) * pageable.getPageSize());
    }

  }

  static class StreamExecution extends MybatisQueryExecution {

    private static final String NO_SURROUNDING_TRANSACTION = "You're trying to execute a streaming query method without a surrounding transaction that keeps the connection open so that the Stream can actually be consumed. Make sure the code consuming the stream uses @Transactional or any other way of declaring a (read-only) transaction.";

    @Override
    protected Object doMybatisExecute(MybatisQuery query, JpaParametersParameterAccessor accessor) {

      if (!SurroundingTransactionDetectorMethodInterceptor.INSTANCE
          .isSurroundingTransactionActive()) {
        throw new InvalidDataAccessApiUsageException(NO_SURROUNDING_TRANSACTION);
      }

      throw new UnsupportedOperationException("Mybatis StreamExecution is not supported.");
    }

  }

  static class SingleEntityExecution extends MybatisQueryExecution {

    @Override
    protected Object doMybatisExecute(MybatisQuery query, JpaParametersParameterAccessor accessor) {
      Object[] values = accessor.getValues();
      if (null == values || values.length == 0) {
        return query.getSqlSessionTemplate().selectOne(query.getQueryMethod().getStatement());
      }

      return query.getSqlSessionTemplate()
          .selectOne(query.getQueryMethod().getStatement(),
              getMybatisParameters(query.getQueryMethod().getParameters(), values));
    }

  }

  static class ModifyingExecution extends MybatisQueryExecution {

    @Override
    protected Object doMybatisExecute(MybatisQuery query, JpaParametersParameterAccessor accessor) {
      Object[] values = accessor.getValues();
      return query.getSqlSessionTemplate()
          .update(query.getQueryMethod().getStatement(),
              getMybatisParameters(query.getQueryMethod().getParameters(), values));
    }

  }

  private static Object getMybatisParameters(JpaParameters parameters, Object[] values) {
    parameters = parameters.getBindableParameters();

    int paramCount = parameters.getNumberOfParameters();
    if (values == null || paramCount == 0) {
      return null;
    } else if (paramCount == 1) {
      JpaParameter parameter = parameters.getParameter(0);
      Class<?> type = parameter.getType();
      int index = parameter.getIndex();
      Object value = values[index];
      if (type.getClassLoader() != null || Map.class.isAssignableFrom(type)) {
        return value;
      } else {
        final Map<String, Object> params = new ParamMap<>();
        String defaultParamName = GENERIC_NAME_PREFIX + 1;
        params.put(parameter.getName().orElse(defaultParamName), value);
        params.put(defaultParamName, value);
        return params;
      }
    } else {
      final Map<String, Object> params = new ParamMap<>();
      for (JpaParameter parameter : parameters) {
        String otherName = GENERIC_NAME_PREFIX + (parameter.getIndex() + 1);
        Object value = values[parameter.getIndex()];
        params.put(parameter.getName().orElse(otherName), value);
        params.put(otherName, value);
      }
      return params;
    }
  }

  private static String getSort(JpaParameters parameters, Object[] values) {
    if (parameters.hasSortParameter()) {
      Sort sort = (Sort) values[parameters.getSortIndex()];
      return null != sort && sort.isSorted() ? sort.toString().replace(":", "") : null;
    }
    if (parameters.hasPageableParameter()) {
      Pageable pageable = (Pageable) values[parameters.getPageableIndex()];
      Sort sort = pageable.getSort();
      return null != sort && sort.isSorted() ? sort.toString().replace(":", "") : null;
    }
    return null;
  }
}

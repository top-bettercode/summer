package org.springframework.data.jpa.repository.query;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.JpaParameters.JpaParameter;
import org.springframework.data.repository.core.support.SurroundingTransactionDetectorMethodInterceptor;
import org.springframework.data.util.ParsingUtils;
import top.bettercode.simpleframework.data.jpa.support.Size;

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
      MybatisParameters mybatisParameters = getParameters(parameters, values);
      Object params = mybatisParameters.parameters;
      Optional<Page<Object>> page = mybatisParameters.getPage();
      return page.<Object>map(objects -> LocalPage.doSelectList(objects,
              () -> query.getSqlSessionTemplate().selectList(statement, params)))
          .orElseGet(() -> query.getSqlSessionTemplate().selectList(statement, params));
    }

  }

  static class PagedExecution extends MybatisQueryExecution {

    @Override
    protected Object doMybatisExecute(MybatisQuery query, JpaParametersParameterAccessor accessor) {
      Object[] values = accessor.getValues();
      String statement = query.getQueryMethod().getStatement();

      JpaParameters parameters = query.getQueryMethod().getParameters();
      MybatisParameters mybatisParameters = getParameters(parameters, values);
      Object params = mybatisParameters.parameters;
      Optional<Page<Object>> page = mybatisParameters.getPage();
      Pageable pageable = mybatisParameters.getPageable();
      if (page.isPresent()) {
        System.err.println(page.get().getPageSize());
        System.err.println(page.get().getPageNum());
        Page<Object> pageResult = LocalPage.doSelectPage(page.get(),
            () -> query.getSqlSessionTemplate().selectList(statement, params));
        return new PageImpl<>(pageResult.getResult(), pageable, pageResult.getTotal());
      } else {
        List<Object> result = query.getSqlSessionTemplate().selectList(statement, params);
        return new PageImpl<>(result, pageable, result.size());
      }
    }
  }

  static class SlicedExecution extends PagedExecution {

    @Override
    protected Object doMybatisExecute(MybatisQuery query, JpaParametersParameterAccessor accessor) {
      PageImpl<?> page = (PageImpl<?>) super.doMybatisExecute(query, accessor);

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
              getParameters(query.getQueryMethod().getParameters(), values).parameters);
    }

  }

  static class ModifyingExecution extends MybatisQueryExecution {

    @Override
    protected Object doMybatisExecute(MybatisQuery query, JpaParametersParameterAccessor accessor) {
      Object[] values = accessor.getValues();
      return query.getSqlSessionTemplate()
          .update(query.getQueryMethod().getStatement(),
              getParameters(query.getQueryMethod().getParameters(), values).parameters);
    }

  }

  private static MybatisParameters getParameters(JpaParameters parameters, Object[] values) {
    Page<Object> page = null;
    Pageable pageable = null;
    int bindableSize = 0;
    final Map<String, Object> paramMap = new ParamMap<>();
    for (JpaParameter parameter : parameters) {
      Class<?> parameterType = parameter.getType();
      int parameterIndex = parameter.getIndex();
      Object value = values[parameterIndex];
      if (Pageable.class.isAssignableFrom(parameterType)) {
        pageable = (Pageable) value;
        if (pageable != Pageable.unpaged()) {
          page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize(), true);
          Sort sort = pageable.getSort();
          page.setOrderBy(convertOrderBy(sort));
        }
      } else if (Size.class.isAssignableFrom(parameterType)) {
        Size size = (Size) value;
        page = new Page<>(1, size.getSize(), false);
        Sort sort = size.getSort();
        page.setOrderBy(convertOrderBy(sort));
      } else if (Sort.class.isAssignableFrom(parameterType)) {
        Sort sort = (Sort) value;
        page = new Page<>();
        page.setCount(false);
        page.setOrderByOnly(true);
        page.setOrderBy(convertOrderBy(sort));
      } else {
        String otherName = GENERIC_NAME_PREFIX + (bindableSize + 1);
        Optional<String> name = parameter.getName();
        name.ifPresent(s -> paramMap.put(s, value));
        paramMap.put(otherName, value);
        paramMap.put(String.valueOf(bindableSize), value);
        bindableSize++;
      }
    }
    Object params =
        bindableSize == 0 ? null : (bindableSize == 1 ? paramMap.get("0") : paramMap);
    return new MybatisParameters(params, page, pageable);
  }

  public static String convertOrderBy(Sort sort) {
    if (sort == null || !sort.isSorted()) {
      return null;
    }
    return sort.stream().map(
            o -> ParsingUtils.reconcatenateCamelCase(o.getProperty(), "_") + " " + o.getDirection())
        .collect(
            Collectors.joining(","));
  }

  private static class MybatisParameters {

    private final Object parameters;

    private final Page<Object> page;

    private final Pageable pageable;

    public MybatisParameters(Object parameters, Page<Object> page, Pageable pageable) {
      this.parameters = parameters;
      this.page = page;
      this.pageable = pageable;
    }

    public Optional<Page<Object>> getPage() {
      return Optional.ofNullable(this.page);
    }

    public Pageable getPageable() {
      return pageable == null ? Pageable.unpaged() : pageable;
    }

  }

  private static class LocalPage extends PageMethod {

    public static <E> List<E> doSelectList(Page<E> page, ISelect select) {
      page.setCount(false);
      setLocalPage(page);
      try {
        select.doSelect();
      } finally {
        clearPage();
      }
      return page.getResult();
    }

    public static <E> Page<E> doSelectPage(Page<E> page, ISelect select) {
      setLocalPage(page);
      try {
        select.doSelect();
      } finally {
        clearPage();
      }
      if (!page.isCount()) {
        page.setTotal(page.getResult().size());
      }
      return page;
    }
  }
}

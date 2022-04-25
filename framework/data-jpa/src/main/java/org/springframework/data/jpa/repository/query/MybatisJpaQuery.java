package org.springframework.data.jpa.repository.query;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.hibernate.query.NativeQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.JpaQueryExecution.ProcedureExecution;
import org.springframework.data.jpa.repository.query.JpaQueryExecution.StreamExecution;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.data.util.ParsingUtils;
import org.springframework.util.Assert;
import top.bettercode.simpleframework.data.jpa.query.mybatis.CountSqlParser;
import top.bettercode.simpleframework.data.jpa.query.mybatis.MybatisParam;
import top.bettercode.simpleframework.data.jpa.query.mybatis.MybatisQuery;
import top.bettercode.simpleframework.data.jpa.query.mybatis.MybatisResultSetHandler;
import top.bettercode.simpleframework.data.jpa.query.mybatis.MybatisResultTransformer;
import top.bettercode.simpleframework.data.jpa.query.mybatis.NestedResultMapType;
import top.bettercode.simpleframework.data.jpa.support.JpaUtil;
import top.bettercode.simpleframework.data.jpa.support.Size;

public class MybatisJpaQuery extends AbstractJpaQuery {

  private final Logger sqlLog = LoggerFactory.getLogger("org.hibernate.SQL");
  private final QueryParameterSetter.QueryMetadataCache metadataCache = new QueryParameterSetter.QueryMetadataCache();
  private final MappedStatement mappedStatement;
  private final MappedStatement countMappedStatement;
  private final CountSqlParser countSqlParser = new CountSqlParser();
  private final NestedResultMapType nestedResultMapType;
  private final MybatisResultTransformer resultTransformer;
  private final boolean isModifyingQuery;

  public MybatisJpaQuery(JpaExtQueryMethod method, EntityManager em) {
    super(method, em);
    this.mappedStatement = method.getMappedStatement();
    SqlCommandType sqlCommandType = this.mappedStatement.getSqlCommandType();
    this.isModifyingQuery =
        SqlCommandType.UPDATE.equals(sqlCommandType) || SqlCommandType.DELETE.equals(sqlCommandType)
            || SqlCommandType.INSERT.equals(sqlCommandType);
    MybatisResultSetHandler.validateResultMaps(this.mappedStatement);
    resultTransformer = new MybatisResultTransformer(this.mappedStatement);

    boolean pageQuery = method.isPageQuery();

    if (pageQuery) {
      String nestedResultMapId = MybatisResultSetHandler.findNestedResultMap(this.mappedStatement);
      if (nestedResultMapId != null) {
        sqlLog.warn(
            "{} may return incorrect paginated data. Please check result maps definition {}.",
            mappedStatement.getId(), nestedResultMapId);
      }
    }
    if (method.isSliceQuery()) {
      nestedResultMapType = MybatisResultSetHandler.findNestedResultMapType(this.mappedStatement);
    } else {
      nestedResultMapType = null;
    }

    MappedStatement countMappedStatement;
    if (pageQuery) {
      try {
        countMappedStatement = this.mappedStatement.getConfiguration()
            .getMappedStatement(this.mappedStatement.getId() + "_COUNT");
      } catch (Exception ignored) {
        countMappedStatement = null;
      }
      this.countMappedStatement = countMappedStatement;
    } else {
      this.countMappedStatement = null;
    }
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

  private static final Pattern ORDER_BY = Pattern.compile(".*order\\s+by\\s+.*", CASE_INSENSITIVE);

  public static String applySorting(String query, Sort sort) {
    Assert.hasText(query, "Query must not be null or empty!");
    if (sort.isUnsorted()) {
      return query;
    }
    StringBuilder builder = new StringBuilder(query);
    if (!ORDER_BY.matcher(query).matches()) {
      builder.append(" order by ");
    } else {
      builder.append(", ");
    }
    builder.append(convertOrderBy(sort));
    return builder.toString();
  }

  @SuppressWarnings("deprecation")
  @Override
  public Query doCreateQuery(JpaParametersParameterAccessor accessor) {
    MybatisParameterBinder parameterBinder = (MybatisParameterBinder) this.parameterBinder.get();
    MybatisParam mybatisParam = parameterBinder.bindParameterObject(accessor);
    BoundSql boundSql = mybatisParam.getBoundSql();
    String queryString = boundSql.getSql();

    Sort sort = accessor.getSort();
    Size size = mybatisParam.getSize();
    String sortedQueryString = applySorting(queryString,
        sort.isUnsorted() && size != null ? size.getSort() : sort);
    Query query = getEntityManager().createNativeQuery(sortedQueryString);
    query.unwrap(NativeQuery.class).setResultTransformer(resultTransformer);
    QueryParameterSetter.QueryMetadata metadata = metadataCache.getMetadata(sortedQueryString,
        query);
    // it is ok to reuse the binding contained in the ParameterBinder although we create a new query String because the
    // parameters in the query do not change.
    return parameterBinder.bindAndPrepare(new MybatisQuery(queryString, query, mybatisParam),
        metadata,
        accessor, mybatisParam);
  }

  @Override
  protected Query doCreateCountQuery(JpaParametersParameterAccessor accessor) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected ParameterBinder createBinder() {
    return new MybatisParameterBinder(getQueryMethod().getParameters(), mappedStatement);
  }

  @Override
  protected JpaQueryExecution getExecution() {
    JpaQueryMethod method = getQueryMethod();
    if (method.isStreamQuery()) {
      return new StreamExecution() {
        @Override
        protected Object doExecute(AbstractJpaQuery query,
            JpaParametersParameterAccessor accessor) {
          try {
            MDC.put("id", mappedStatement.getId());
            return super.doExecute(query, accessor);
          } finally {
            MDC.remove("id");
          }
        }
      };
    } else if (method.isProcedureQuery()) {
      return new ProcedureExecution() {
        @Override
        protected Object doExecute(AbstractJpaQuery jpaQuery,
            JpaParametersParameterAccessor accessor) {
          try {
            MDC.put("id", mappedStatement.getId());
            return super.doExecute(jpaQuery, accessor);
          } finally {
            MDC.remove("id");
          }
        }
      };
    } else if (method.isCollectionQuery()) {
      return new JpaQueryExecution.CollectionExecution() {
        @Override
        protected Object doExecute(AbstractJpaQuery query,
            JpaParametersParameterAccessor accessor) {
          try {
            MDC.put("id", mappedStatement.getId());
            List<?> result = (List<?>) super.doExecute(query, accessor);
            if (sqlLog.isDebugEnabled()) {
              sqlLog.debug("{} rows retrieved", result.size());
            }
            return result;
          } finally {
            MDC.remove("id");
          }
        }
      };
    } else if (method.isSliceQuery()) {
      return new JpaQueryExecution.SlicedExecution() {
        @Override
        protected Object doExecute(AbstractJpaQuery query,
            JpaParametersParameterAccessor accessor) {
          try {
            MDC.put("id", mappedStatement.getId());
            Pageable pageable = accessor.getPageable();
            if (pageable.isPaged() && nestedResultMapType != null) {
              if (nestedResultMapType.isCollection()) {
                throw new UnsupportedOperationException(nestedResultMapType.getNestedResultMapId()
                    + " collection resultmap not support page query");
              } else {
                sqlLog.warn(
                    "{} may return incorrect paginated data. Please check result maps definition {}.",
                    mappedStatement.getId(), nestedResultMapType.getNestedResultMapId());
              }
            }
            SliceImpl<?> result = (SliceImpl<?>) super.doExecute(query, accessor);
            if (sqlLog.isDebugEnabled()) {
              sqlLog.debug("total: {} rows", result.getNumberOfElements());
              sqlLog.debug("{} rows retrieved", result.getSize());
            }
            return result;
          } finally {
            MDC.remove("id");
          }
        }
      };
    } else if (method.isPageQuery()) {
      return new JpaQueryExecution.PagedExecution() {
        @Override
        protected Object doExecute(AbstractJpaQuery repositoryQuery,
            JpaParametersParameterAccessor accessor) {
          try {
            MDC.put("id", mappedStatement.getId());
            MybatisQuery mybatisQuery = (MybatisQuery) repositoryQuery.createQuery(accessor);
            long total;
            List<?> resultList;
            if (accessor.getPageable().isPaged()) {
              JpaQueryMethod method = getQueryMethod();
              String countQueryString = null;
              MybatisParam mybatisParam = mybatisQuery.getMybatisParam();
              if (countMappedStatement != null) {
                BoundSql boundSql = countMappedStatement.getBoundSql(
                    mybatisParam.getParameterObject());
                countQueryString = boundSql.getSql();
              }
              String queryString =
                  countQueryString != null ? countQueryString : countSqlParser.getSmartCountSql(
                      mybatisQuery.getQueryString());
              EntityManager em = getEntityManager();

              Query countQuery = em.createNativeQuery(queryString);

              QueryParameterSetter.QueryMetadata metadata = metadataCache.getMetadata(queryString,
                  countQuery);

              ((MybatisParameterBinder) parameterBinder.get()).bind(metadata.withQuery(countQuery),
                  mybatisParam);

              countQuery =
                  method.applyHintsToCountQuery() ? applyHints(countQuery, method) : countQuery;

              List<?> totals = countQuery.getResultList();
              total =
                  totals.size() == 1 ? JpaUtil.convert(totals.get(0), Long.class) : totals.size();
              if (sqlLog.isDebugEnabled()) {
                sqlLog.debug("total: {} rows", total);
              }
              if (total > 0 && total > accessor.getPageable().getOffset()) {
                resultList = mybatisQuery.getResultList();
                if (sqlLog.isDebugEnabled()) {
                  sqlLog.debug("{} rows retrieved", resultList.size());
                }
              } else {
                resultList = Collections.emptyList();
              }
            } else {
              resultList = mybatisQuery.getResultList();
              if (sqlLog.isDebugEnabled()) {
                sqlLog.debug("{} rows retrieved", resultList.size());
              }
              total = resultList.size();
            }

            return PageableExecutionUtils.getPage(resultList, accessor.getPageable(), () -> total);
          } finally {
            MDC.remove("id");
          }
        }
      };
    } else if (isModifyingQuery || method.isModifyingQuery()) {
      return new JpaQueryExecution.ModifyingExecution(method, getEntityManager()) {
        @Override
        protected Object doExecute(AbstractJpaQuery query,
            JpaParametersParameterAccessor accessor) {
          try {
            MDC.put("id", mappedStatement.getId());
            Object result = super.doExecute(query, accessor);
            if (sqlLog.isDebugEnabled()) {
              sqlLog.debug("{} row affected", result);
            }
            return result;
          } finally {
            MDC.remove("id");
          }
        }
      };
    } else {
      return new JpaQueryExecution.SingleEntityExecution() {
        @Override
        protected Object doExecute(AbstractJpaQuery query,
            JpaParametersParameterAccessor accessor) {
          try {
            MDC.put("id", mappedStatement.getId());
            Object result = super.doExecute(query, accessor);
            if (sqlLog.isDebugEnabled()) {
              sqlLog.debug("{} rows retrieved", result == null ? 0 : 1);
            }
            return result;
          } finally {
            MDC.remove("id");
          }
        }
      };
    }
  }
}

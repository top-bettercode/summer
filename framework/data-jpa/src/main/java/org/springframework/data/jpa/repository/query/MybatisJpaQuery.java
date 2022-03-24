package org.springframework.data.jpa.repository.query;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.JpaQueryExecution.StreamExecution;
import top.bettercode.simpleframework.data.jpa.query.mybatis.CountSqlParser;
import top.bettercode.simpleframework.data.jpa.query.mybatis.JpaExtQueryMethod;
import top.bettercode.simpleframework.data.jpa.query.mybatis.MybatisParam;
import top.bettercode.simpleframework.data.jpa.query.mybatis.TuplesResultHandler;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.data.util.ParsingUtils;
import org.springframework.util.Assert;
import top.bettercode.simpleframework.data.jpa.query.mybatis.MybatisQuery;
import top.bettercode.simpleframework.data.jpa.support.JpaUtil;
import top.bettercode.simpleframework.data.jpa.support.Size;

public class MybatisJpaQuery extends AbstractJpaQuery {

  private final Logger sqlLog = LoggerFactory.getLogger("org.hibernate.SQL");
  private final QueryParameterSetter.QueryMetadataCache metadataCache = new QueryParameterSetter.QueryMetadataCache();
  private final MappedStatement mappedStatement;
  private final MappedStatement countMappedStatement;
  private final TuplesResultHandler tuplesResultHandler;
  private final CountSqlParser countSqlParser = new CountSqlParser();


  public MybatisJpaQuery(JpaExtQueryMethod method, EntityManager em) {
    super(method, em);
    this.mappedStatement = method.getMappedStatement();
    this.tuplesResultHandler = new TuplesResultHandler(mappedStatement);

    boolean pageQuery = method.isPageQuery();

    if (pageQuery || method.isStreamQuery()) {
      String nestedResultMap = this.tuplesResultHandler.findNestedResultMap(pageQuery);
      if (nestedResultMap != null) {
        sqlLog.warn(
            "{} may return incorrect " + (pageQuery ? "paginated" : "streamed")
                + " data. Please check result maps definition {}.",
            mappedStatement.getId(), nestedResultMap);
      }
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
    Query query = getEntityManager().createNativeQuery(sortedQueryString, Tuple.class);
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

  @SuppressWarnings({"unchecked"})
  @Override
  protected JpaQueryExecution getExecution() {
    JpaQueryMethod method = getQueryMethod();
    if (method.isStreamQuery()) {
      return new StreamExecution() {
        @Override
        protected Object doExecute(AbstractJpaQuery query,
            JpaParametersParameterAccessor accessor) {
          return ((Stream<Tuple>) super.doExecute(query, accessor)).map(
              tuplesResultHandler::handleTuple);
        }
      };
    } else if (method.isProcedureQuery()) {
      throw new UnsupportedOperationException("Mybatis ProcedureExecution is not supported.");
    } else if (method.isCollectionQuery()) {
      return new JpaQueryExecution.CollectionExecution() {
        @Override
        protected Object doExecute(AbstractJpaQuery query,
            JpaParametersParameterAccessor accessor) {
          Object result = super.doExecute(query, accessor);
          List<Tuple> tuples = (List<Tuple>) result;
          if (sqlLog.isDebugEnabled()) {
            sqlLog.debug("{} rows retrieved", tuples.size());
          }
          return tuplesResultHandler.handleTuples(tuples);
        }
      };
    } else if (method.isSliceQuery()) {
      return new JpaQueryExecution.SlicedExecution() {
        @Override
        protected Object doExecute(AbstractJpaQuery query,
            JpaParametersParameterAccessor accessor) {
          Pageable pageable = accessor.getPageable();
          Query createQuery = query.createQuery(accessor);

          int pageSize = 0;
          if (pageable.isPaged()) {
            pageSize = pageable.getPageSize();
            createQuery.setMaxResults(pageSize + 1);
          }

          List<Tuple> resultList = createQuery.getResultList();
          if (sqlLog.isDebugEnabled()) {
            sqlLog.debug("{} rows retrieved", resultList.size());
          }
          boolean hasNext = pageable.isPaged() && resultList.size() > pageSize;
          List<Object> objects = tuplesResultHandler.handleTuples(resultList);
          return new SliceImpl<>(hasNext ? objects.subList(0, pageSize) : objects, pageable,
              hasNext);
        }
      };
    } else if (method.isPageQuery()) {
      return new JpaQueryExecution.PagedExecution() {
        @Override
        protected Object doExecute(AbstractJpaQuery repositoryQuery,
            JpaParametersParameterAccessor accessor) {
          MybatisQuery mybatisQuery = (MybatisQuery) repositoryQuery.createQuery(accessor);
          long total;
          List<Tuple> resultList;
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
            if (total > 0) {
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

          return PageableExecutionUtils.getPage(
              tuplesResultHandler.handleTuples(resultList), accessor.getPageable(),
              () -> total);
        }
      };
    } else if (method.isModifyingQuery()) {
      return new JpaQueryExecution.ModifyingExecution(method, getEntityManager()) {
        @Override
        protected Object doExecute(AbstractJpaQuery query,
            JpaParametersParameterAccessor accessor) {
          Object result = super.doExecute(query, accessor);
          if (sqlLog.isDebugEnabled()) {
            sqlLog.debug("{} row affected", result);
          }
          return result;
        }
      };
    } else {
      return new JpaQueryExecution.SingleEntityExecution() {
        @Override
        protected Object doExecute(AbstractJpaQuery query,
            JpaParametersParameterAccessor accessor) {
          Tuple tuple = (Tuple) super.doExecute(query, accessor);
          if (sqlLog.isDebugEnabled()) {
            sqlLog.debug("{} rows retrieved", tuple == null ? 0 : 1);
          }
          return tuplesResultHandler.handleTuple(tuple);
        }
      };
    }
  }
}

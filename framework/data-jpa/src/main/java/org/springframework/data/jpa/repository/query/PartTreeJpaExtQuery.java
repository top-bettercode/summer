package org.springframework.data.jpa.repository.query;

import top.bettercode.simpleframework.data.jpa.config.JpaExtProperties;
import top.bettercode.simpleframework.data.jpa.support.DefaultSoftDeleteSupport;
import top.bettercode.simpleframework.data.jpa.support.SoftDeleteSupport;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.query.JpaQueryExecution.ExistsExecution;
import org.springframework.data.jpa.repository.query.ParameterMetadataProvider.ParameterMetadata;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.repository.query.ReturnedType;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.lang.Nullable;

/**
 * @author Peter Wu
 */
public class PartTreeJpaExtQuery extends AbstractJpaQuery {

  private final PartTree tree;
  private final JpaParameters parameters;

  private final QueryPreparer query;
  private final QueryPreparer countQuery;
  private final EntityManager em;
  private final EscapeCharacter escape;
  private final SoftDeleteSupport softDeleteSupport;

  /**
   * Creates a new {@link PartTreeJpaQuery}.
   *
   * @param method              must not be {@literal null}.
   * @param em                  must not be {@literal null}.
   * @param persistenceProvider must not be {@literal null}.
   * @param escape              escape
   */
  PartTreeJpaExtQuery(JpaQueryMethod method, EntityManager em,
      PersistenceProvider persistenceProvider, EscapeCharacter escape,
      JpaExtProperties jpaExtProperties) {

    super(method, em);

    this.em = em;
    this.escape = escape;
    Class<?> domainClass = method.getEntityInformation().getJavaType();
    this.softDeleteSupport = new DefaultSoftDeleteSupport(jpaExtProperties, domainClass);
    this.parameters = method.getParameters();

    boolean recreationRequired =
        parameters.hasDynamicProjection() || parameters.potentiallySortsDynamically();

    try {

      this.tree = new PartTree(method.getName(), domainClass);
      this.countQuery = new CountQueryPreparer(recreationRequired);
      this.query = tree.isCountProjection() ? countQuery : new QueryPreparer(recreationRequired);

    } catch (Exception o_O) {
      throw new IllegalArgumentException(
          String.format("Failed to create query for method %s! %s", method, o_O.getMessage()), o_O);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.jpa.repository.query.AbstractJpaQuery#getExecution()
   */
  @Override
  protected JpaQueryExecution getExecution() {

    if (this.tree.isDelete()) {
      return new SoftDeleteExecution(em, softDeleteSupport);
    } else if (this.tree.isExistsProjection()) {
      return new ExistsExecution();
    }

    return super.getExecution();
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.jpa.repository.query.AbstractJpaQuery#doCreateQuery(JpaParametersParameterAccessor)
   */
  @Override
  public Query doCreateQuery(JpaParametersParameterAccessor accessor) {
    return query.createQuery(accessor);
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.jpa.repository.query.AbstractJpaQuery#doCreateCountQuery(JpaParametersParameterAccessor)
   */
  @Override
  @SuppressWarnings("unchecked")
  public TypedQuery<Long> doCreateCountQuery(JpaParametersParameterAccessor accessor) {
    return (TypedQuery<Long>) countQuery.createQuery(accessor);
  }

  /**
   * Query preparer to create {@link CriteriaQuery} instances and potentially cache them.
   *
   * @author Oliver Gierke
   * @author Thomas Darimont
   */
  private class QueryPreparer {

    private final @Nullable
    CriteriaQuery<?> cachedCriteriaQuery;
    private final @Nullable
    ParameterBinder cachedParameterBinder;
    private final QueryParameterSetter.QueryMetadataCache metadataCache = new QueryParameterSetter.QueryMetadataCache();

    QueryPreparer(boolean recreateQueries) {

      JpaQueryCreator creator = createCreator(null);

      if (recreateQueries) {
        this.cachedCriteriaQuery = null;
        this.cachedParameterBinder = null;
      } else {
        this.cachedCriteriaQuery = creator.createQuery();
        this.cachedParameterBinder = getBinder(creator.getParameterExpressions());
      }
    }

    /**
     * Creates a new {@link Query} for the given parameter values.
     */
    public Query createQuery(JpaParametersParameterAccessor accessor) {

      CriteriaQuery<?> criteriaQuery = cachedCriteriaQuery;
      ParameterBinder parameterBinder = cachedParameterBinder;

      if (cachedCriteriaQuery == null || accessor.hasBindableNullValue()) {
        JpaQueryCreator creator = createCreator(accessor);
        criteriaQuery = creator.createQuery(getDynamicSort(accessor));
        List<ParameterMetadata<?>> expressions = creator.getParameterExpressions();
        parameterBinder = getBinder(expressions);
      }

      if (parameterBinder == null) {
        throw new IllegalStateException("ParameterBinder is null!");
      }

      TypedQuery<?> query = createQuery(criteriaQuery);

      return restrictMaxResultsIfNecessary(
          invokeBinding(parameterBinder, query, accessor, this.metadataCache));
    }

    /**
     * Restricts the max results of the given {@link Query} if the current {@code tree} marks this
     * {@code query} as limited.
     */
    @SuppressWarnings("ConstantConditions")
    private Query restrictMaxResultsIfNecessary(Query query) {

      if (tree.isLimiting()) {

        if (query.getMaxResults() != Integer.MAX_VALUE) {
          /*
           * In order to return the correct results, we have to adjust the first result offset to be returned if:
           * - a Pageable parameter is present
           * - AND the requested page number > 0
           * - AND the requested page size was bigger than the derived result limitation via the First/Top keyword.
           */
          if (query.getMaxResults() > tree.getMaxResults() && query.getFirstResult() > 0) {
            query.setFirstResult(
                query.getFirstResult() - (query.getMaxResults() - tree.getMaxResults()));
          }
        }

        query.setMaxResults(tree.getMaxResults());
      }

      if (tree.isExistsProjection()) {
        query.setMaxResults(1);
      }

      return query;
    }

    /**
     * Checks whether we are working with a cached {@link CriteriaQuery} and synchronizes the
     * creation of a {@link TypedQuery} instance from it. This is due to non-thread-safety in the
     * {@link CriteriaQuery} implementation of some persistence providers (i.e. Hibernate in this
     * case), see DATAJPA-396.
     *
     * @param criteriaQuery must not be {@literal null}.
     */
    private TypedQuery<?> createQuery(CriteriaQuery<?> criteriaQuery) {

      if (this.cachedCriteriaQuery != null) {
        synchronized (this.cachedCriteriaQuery) {
          return getEntityManager().createQuery(criteriaQuery);
        }
      }

      return getEntityManager().createQuery(criteriaQuery);
    }

    protected JpaQueryCreator createCreator(@Nullable JpaParametersParameterAccessor accessor) {

      EntityManager entityManager = getEntityManager();

      CriteriaBuilder builder = entityManager.getCriteriaBuilder();
      ResultProcessor processor = getQueryMethod().getResultProcessor();

      ParameterMetadataProvider provider;
      ReturnedType returnedType;

      if (accessor != null) {
        provider = new ParameterMetadataProvider(builder, accessor, escape);
        returnedType = processor.withDynamicProjection(accessor).getReturnedType();
      } else {
        provider = new ParameterMetadataProvider(builder, parameters, escape);
        returnedType = processor.getReturnedType();
      }

      return new JpaExtQueryCreator(tree, returnedType, builder, provider, softDeleteSupport);
    }

    /**
     * Invokes parameter binding on the given {@link TypedQuery}.
     */
    protected Query invokeBinding(ParameterBinder binder, TypedQuery<?> query,
        JpaParametersParameterAccessor accessor,
        QueryParameterSetter.QueryMetadataCache metadataCache) {

      QueryParameterSetter.QueryMetadata metadata = metadataCache.getMetadata("query", query);

      return binder.bindAndPrepare(query, metadata, accessor);
    }

    private ParameterBinder getBinder(List<ParameterMetadata<?>> expressions) {
      return ParameterBinderFactory.createCriteriaBinder(parameters, expressions);
    }

    private Sort getDynamicSort(JpaParametersParameterAccessor accessor) {

      return parameters.potentiallySortsDynamically() //
          ? accessor.getSort() //
          : Sort.unsorted();
    }
  }

  /**
   * Special {@link QueryPreparer} to create count queries.
   *
   * @author Oliver Gierke
   * @author Thomas Darimont
   */
  private class CountQueryPreparer extends QueryPreparer {

    CountQueryPreparer(boolean recreateQueries) {
      super(recreateQueries);
    }

    @Override
    protected JpaQueryCreator createCreator(@Nullable JpaParametersParameterAccessor accessor) {

      EntityManager entityManager = getEntityManager();
      CriteriaBuilder builder = entityManager.getCriteriaBuilder();

      ParameterMetadataProvider provider;

      if (accessor != null) {
        provider = new ParameterMetadataProvider(builder, accessor, escape);
      } else {
        provider = new ParameterMetadataProvider(builder, parameters, escape);
      }

      return new JpaExtCountQueryCreator(tree,
          getQueryMethod().getResultProcessor().getReturnedType(), builder, provider,
          softDeleteSupport);
    }

    /**
     * Customizes binding by skipping the pagination.
     */
    @Override
    protected Query invokeBinding(ParameterBinder binder, TypedQuery<?> query,
        JpaParametersParameterAccessor accessor,
        QueryParameterSetter.QueryMetadataCache metadataCache) {

      QueryParameterSetter.QueryMetadata metadata = metadataCache.getMetadata("countquery", query);

      return binder.bind(query, metadata, accessor);
    }
  }

}
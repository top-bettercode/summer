package org.springframework.data.jpa.repository.query;

import cn.bestwu.simpleframework.data.jpa.config.JpaExtProperties;
import java.lang.reflect.Method;
import javax.persistence.EntityManager;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.provider.QueryExtractor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.mybatis.JpaExtQueryMethod;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Query lookup strategy to execute finders.
 *
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Mark Paluch
 */
public final class JpaExtQueryLookupStrategy {

  /**
   * Private constructor to prevent instantiation.
   */
  private JpaExtQueryLookupStrategy() {
  }

  /**
   * Base class for {@link QueryLookupStrategy} implementations that need access to an {@link
   * EntityManager}.
   *
   * @author Oliver Gierke
   * @author Thomas Darimont
   */
  private abstract static class AbstractQueryLookupStrategy implements QueryLookupStrategy {

    private final EntityManager em;
    private final QueryExtractor provider;

    /**
     * Creates a new {@link AbstractQueryLookupStrategy}.
     */
    public AbstractQueryLookupStrategy(EntityManager em, QueryExtractor extractor) {

      this.em = em;
      this.provider = extractor;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.QueryLookupStrategy#resolveQuery(java.lang.reflect.Method, org.springframework.data.repository.core.RepositoryMetadata, org.springframework.data.projection.ProjectionFactory, org.springframework.data.repository.core.NamedQueries)
     */
    @Override
    public final RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata,
        ProjectionFactory factory,
        NamedQueries namedQueries) {
      return resolveQuery(new JpaExtQueryMethod(method, metadata, factory, provider), em,
          namedQueries);
    }

    protected abstract RepositoryQuery resolveQuery(JpaExtQueryMethod method, EntityManager em,
        NamedQueries namedQueries);
  }

  /**
   * {@link QueryLookupStrategy} to create a query from the method name.
   *
   * @author Oliver Gierke
   * @author Thomas Darimont
   */
  private static class CreateQueryLookupStrategy extends AbstractQueryLookupStrategy {

    private final PersistenceProvider persistenceProvider;
    private final EscapeCharacter escape;
    private final JpaExtProperties jpaExtProperties;

    public CreateQueryLookupStrategy(EntityManager em, QueryExtractor extractor,
        EscapeCharacter escape,
        JpaExtProperties jpaExtProperties) {

      super(em, extractor);
      this.persistenceProvider = PersistenceProvider.fromEntityManager(em);
      this.escape = escape;
      this.jpaExtProperties = jpaExtProperties;
    }

    @Override
    protected RepositoryQuery resolveQuery(JpaExtQueryMethod method, EntityManager em,
        NamedQueries namedQueries) {
      return new PartTreeJpaExtQuery(method, em, persistenceProvider, escape, jpaExtProperties);
    }
  }

  /**
   * {@link QueryLookupStrategy} that tries to detect a declared query declared via {@link Query}
   * annotation followed by a JPA named query lookup.
   *
   * @author Oliver Gierke
   * @author Thomas Darimont
   */
  private static class DeclaredQueryLookupStrategy extends AbstractQueryLookupStrategy {

    private final QueryMethodEvaluationContextProvider evaluationContextProvider;
    private final SqlSessionTemplate sqlSessionTemplate;

    /**
     * Creates a new {@link DeclaredQueryLookupStrategy}.
     */
    public DeclaredQueryLookupStrategy(EntityManager em, SqlSessionTemplate sqlSessionTemplate,
        QueryExtractor extractor,
        QueryMethodEvaluationContextProvider evaluationContextProvider) {

      super(em, extractor);
      this.evaluationContextProvider = evaluationContextProvider;
      this.sqlSessionTemplate = sqlSessionTemplate;
    }

    public SqlSessionTemplate getSqlSessionTemplate() {
      return sqlSessionTemplate;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.jpa.repository.query.JpaQueryLookupStrategy.AbstractQueryLookupStrategy#resolveQuery(org.springframework.data.jpa.repository.query.JpaQueryMethod, javax.persistence.EntityManager, org.springframework.data.repository.core.NamedQueries)
     */
    @Override
    protected RepositoryQuery resolveQuery(JpaExtQueryMethod method, EntityManager em,
        NamedQueries namedQueries) {

      if (method.isMybatisQuery()) {
        return new MybatisQuery(method, em, sqlSessionTemplate);
      }

      RepositoryQuery query = JpaQueryFactory.INSTANCE
          .fromQueryAnnotation(method, em, evaluationContextProvider);

      if (null != query) {
        return query;
      }

      query = JpaQueryFactory.INSTANCE.fromProcedureAnnotation(method, em);

      if (null != query) {
        return query;
      }

      String name = method.getNamedQueryName();
      if (namedQueries.hasQuery(name)) {
        return JpaQueryFactory.INSTANCE
            .fromMethodWithQueryString(method, em, namedQueries.getQuery(name),
                evaluationContextProvider);
      }

      query = NamedQuery.lookupFrom(method, em);

      if (null != query) {
        return query;
      }

      throw new IllegalStateException(
          String.format("Did neither find a NamedQuery nor an annotated query for method %s!",
              method));
    }
  }

  /**
   * {@link QueryLookupStrategy} to try to detect a declared query first ( {@link
   * org.springframework.data.jpa.repository.Query}, JPA named query). In case none is found we fall
   * back on query creation.
   *
   * @author Oliver Gierke
   * @author Thomas Darimont
   */
  private static class CreateIfNotFoundQueryLookupStrategy extends AbstractQueryLookupStrategy {

    private final DeclaredQueryLookupStrategy lookupStrategy;
    private final CreateQueryLookupStrategy createStrategy;

    /**
     * Creates a new {@link CreateIfNotFoundQueryLookupStrategy}.
     */
    public CreateIfNotFoundQueryLookupStrategy(EntityManager em, QueryExtractor extractor,
        CreateQueryLookupStrategy createStrategy, DeclaredQueryLookupStrategy lookupStrategy) {

      super(em, extractor);

      this.createStrategy = createStrategy;
      this.lookupStrategy = lookupStrategy;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.jpa.repository.query.JpaQueryLookupStrategy.AbstractQueryLookupStrategy#resolveQuery(org.springframework.data.jpa.repository.query.JpaQueryMethod, javax.persistence.EntityManager, org.springframework.data.repository.core.NamedQueries)
     */
    @Override
    protected RepositoryQuery resolveQuery(JpaExtQueryMethod method, EntityManager em,
        NamedQueries namedQueries) {

      try {
        return lookupStrategy.resolveQuery(method, em, namedQueries);
      } catch (IllegalStateException e) {
        try {
          return createStrategy.resolveQuery(method, em, namedQueries);
        } catch (Exception e1) {
          return new MybatisQuery(method, em, lookupStrategy.getSqlSessionTemplate());
        }
      }
    }
  }

  /**
   * Creates a {@link QueryLookupStrategy} for the given {@link EntityManager} and {@link Key}.
   *
   * @param em must not be {@literal null}.
   * @param key may be {@literal null}.
   * @param extractor must not be {@literal null}.
   * @param evaluationContextProvider must not be {@literal null}.
   * @param escape escape
   * @param jpaExtProperties jpaExtProperties
   * @param sqlSessionTemplate sqlSessionTemplate
   * @return QueryLookupStrategy
   */
  public static QueryLookupStrategy create(EntityManager em, SqlSessionTemplate sqlSessionTemplate,
      @Nullable Key key,
      QueryExtractor extractor,
      QueryMethodEvaluationContextProvider evaluationContextProvider,
      EscapeCharacter escape,
      JpaExtProperties jpaExtProperties) {

    Assert.notNull(em, "EntityManager must not be null!");
    Assert.notNull(extractor, "QueryExtractor must not be null!");
    Assert.notNull(evaluationContextProvider, "EvaluationContextProvider must not be null!");

    switch (key != null ? key : Key.CREATE_IF_NOT_FOUND) {
      case CREATE:
        return new CreateQueryLookupStrategy(em, extractor, escape, jpaExtProperties);
      case USE_DECLARED_QUERY:
        return new DeclaredQueryLookupStrategy(em, sqlSessionTemplate, extractor,
            evaluationContextProvider);
      case CREATE_IF_NOT_FOUND:
        return new CreateIfNotFoundQueryLookupStrategy(em, extractor,
            new CreateQueryLookupStrategy(em, extractor, escape, jpaExtProperties),
            new DeclaredQueryLookupStrategy(em, sqlSessionTemplate, extractor,
                evaluationContextProvider));
      default:
        throw new IllegalArgumentException(
            String.format("Unsupported query lookup strategy %s!", key));
    }
  }
}
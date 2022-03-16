package org.springframework.data.jpa.repository.query;

import java.lang.reflect.Method;
import javax.persistence.EntityManager;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import top.bettercode.simpleframework.data.jpa.config.JpaExtProperties;

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
      return resolveQuery(
          new JpaExtQueryMethod(method, metadata, factory, provider), em, namedQueries);
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

    private final EscapeCharacter escape;
    private final JpaExtProperties jpaExtProperties;

    public CreateQueryLookupStrategy(EntityManager em, QueryExtractor extractor,
        EscapeCharacter escape,
        JpaExtProperties jpaExtProperties) {

      super(em, extractor);
      this.escape = escape;
      this.jpaExtProperties = jpaExtProperties;
    }

    @Override
    protected RepositoryQuery resolveQuery(JpaExtQueryMethod method, EntityManager em,
        NamedQueries namedQueries) {
      return new PartTreeJpaExtQuery(method, em, escape, jpaExtProperties);
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
    private final Configuration configuration;

    public DeclaredQueryLookupStrategy(EntityManager em, Configuration configuration,
        QueryExtractor extractor,
        QueryMethodEvaluationContextProvider evaluationContextProvider) {

      super(em, extractor);
      this.evaluationContextProvider = evaluationContextProvider;
      this.configuration = configuration;
    }

    public Configuration getConfiguration() {
      return configuration;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.jpa.repository.query.JpaQueryLookupStrategy.AbstractQueryLookupStrategy#resolveQuery(org.springframework.data.jpa.repository.query.JpaQueryMethod, javax.persistence.EntityManager, org.springframework.data.repository.core.NamedQueries)
     */
    @Override
    protected RepositoryQuery resolveQuery(JpaExtQueryMethod method, EntityManager em,
        NamedQueries namedQueries) {

      if (method.isMybatisQuery()) {
        return new MybatisJpaQuery(method, em,
            configuration.getMappedStatement(method.getStatement()));
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

    private final Logger log = LoggerFactory.getLogger(CreateIfNotFoundQueryLookupStrategy.class);
    private final DeclaredQueryLookupStrategy lookupStrategy;
    private final CreateQueryLookupStrategy createStrategy;

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
          if (log.isDebugEnabled()) {
            log.debug(e1.getMessage(), e1);
          }
          return new MybatisJpaQuery(method, em,
              lookupStrategy.getConfiguration().getMappedStatement(method.getStatement()));
        }
      }
    }
  }

  /**
   * Creates a {@link QueryLookupStrategy} for the given {@link EntityManager} and {@link Key}.
   *
   * @param em                        must not be {@literal null}.
   * @param key                       may be {@literal null}.
   * @param extractor                 must not be {@literal null}.
   * @param evaluationContextProvider must not be {@literal null}.
   * @param escape                    escape
   * @param jpaExtProperties          jpaExtProperties
   * @param configuration             configuration
   * @return QueryLookupStrategy
   */
  public static QueryLookupStrategy create(EntityManager em, Configuration configuration,
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
        return new DeclaredQueryLookupStrategy(em, configuration, extractor,
            evaluationContextProvider);
      case CREATE_IF_NOT_FOUND:
        return new CreateIfNotFoundQueryLookupStrategy(em, extractor,
            new CreateQueryLookupStrategy(em, extractor, escape, jpaExtProperties),
            new DeclaredQueryLookupStrategy(em, configuration, extractor,
                evaluationContextProvider));
      default:
        throw new IllegalArgumentException(
            String.format("Unsupported query lookup strategy %s!", key));
    }
  }
}
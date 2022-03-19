package org.springframework.data.jpa.repository.support;

import static org.springframework.data.querydsl.QuerydslUtils.QUERY_DSL_PRESENT;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.jpa.projection.CollectionAwareProjectionFactory;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.provider.QueryExtractor;
import org.springframework.data.jpa.repository.query.AbstractJpaQuery;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.query.JpaExtQueryLookupStrategy;
import org.springframework.data.jpa.repository.query.JpaQueryMethod;
import org.springframework.data.jpa.util.JpaMetamodel;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.QueryCreationListener;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.RepositoryFragment;
import org.springframework.data.repository.core.support.SurroundingTransactionDetectorMethodInterceptor;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.ReturnedType;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import top.bettercode.simpleframework.data.jpa.JpaExtRepository;
import top.bettercode.simpleframework.data.jpa.config.JpaExtProperties;
import top.bettercode.simpleframework.data.jpa.querydsl.QuerydslJpaExtPredicateExecutor;
import top.bettercode.simpleframework.data.jpa.support.SimpleJpaExtRepository;

/**
 * implementation of a custom {@link JpaRepositoryFactory} to use a custom repository base class.
 *
 * @author Peter Wu
 */
public class JpaExtRepositoryFactory extends RepositoryFactorySupport {

  private final JpaExtProperties jpaExtProperties;
  private final Configuration configuration;
  private final EntityManager entityManager;
  private final QueryExtractor extractor;
  private final CrudMethodMetadataPostProcessor crudMethodMetadataPostProcessor;

  private EntityPathResolver entityPathResolver;
  private EscapeCharacter escapeCharacter = EscapeCharacter.DEFAULT;

  public JpaExtRepositoryFactory(EntityManager entityManager,
      Configuration configuration, JpaExtProperties jpaExtProperties) {
    Assert.notNull(entityManager, "EntityManager must not be null!");

    this.entityManager = entityManager;
    this.extractor = PersistenceProvider.fromEntityManager(entityManager);
    this.crudMethodMetadataPostProcessor = new CrudMethodMetadataPostProcessor();
    this.entityPathResolver = SimpleEntityPathResolver.INSTANCE;

    addRepositoryProxyPostProcessor(crudMethodMetadataPostProcessor);
    addRepositoryProxyPostProcessor((factory, repositoryInformation) -> {

      if (hasMethodReturningStream(repositoryInformation.getRepositoryInterface())) {
        factory.addAdvice(SurroundingTransactionDetectorMethodInterceptor.INSTANCE);
      }
    });

    if (extractor.equals(PersistenceProvider.ECLIPSELINK)) {
      addQueryCreationListener(new EclipseLinkProjectionQueryCreationListener(entityManager));
    }
    this.configuration = configuration;
    this.jpaExtProperties = jpaExtProperties;
  }

  @Override
  public void setBeanClassLoader(ClassLoader classLoader) {

    super.setBeanClassLoader(classLoader);
    this.crudMethodMetadataPostProcessor.setBeanClassLoader(classLoader);
  }

  /**
   * Configures the {@link EntityPathResolver} to be used. Defaults to {@link
   * SimpleEntityPathResolver#INSTANCE}.
   *
   * @param entityPathResolver must not be {@literal null}.
   */
  public void setEntityPathResolver(EntityPathResolver entityPathResolver) {

    Assert.notNull(entityPathResolver, "EntityPathResolver must not be null!");

    this.entityPathResolver = entityPathResolver;
  }

  /**
   * Configures the escape character to be used for like-expressions created for derived queries.
   *
   * @param escapeCharacter a character used for escaping in certain like expressions.
   */
  public void setEscapeCharacter(EscapeCharacter escapeCharacter) {
    this.escapeCharacter = escapeCharacter;
  }

  @Override
  protected final JpaRepositoryImplementation<?, ?> getTargetRepository(
      RepositoryInformation information) {

    JpaRepositoryImplementation<?, ?> repository = getTargetRepository(information, entityManager);
    repository.setRepositoryMethodMetadata(crudMethodMetadataPostProcessor.getCrudMethodMetadata());
    repository.setEscapeCharacter(escapeCharacter);

    return repository;
  }

  /**
   * Callback to create a {@link JpaExtRepository} instance with the given {@link EntityManager}
   *
   * @param information   will never be {@literal null}.
   * @param entityManager will never be {@literal null}.
   * @return JpaRepositoryImplementation
   */
  protected JpaRepositoryImplementation<?, ?> getTargetRepository(RepositoryInformation information,
      EntityManager entityManager) {

    JpaEntityInformation<?, Serializable> entityInformation = getEntityInformation(
        information.getDomainType());
    Object repository = getTargetRepositoryViaReflection(information, jpaExtProperties,
        entityInformation, entityManager);

    Assert.isInstanceOf(SimpleJpaExtRepository.class, repository);

    return (JpaRepositoryImplementation<?, ?>) repository;
  }


  @Override
  protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
    return SimpleJpaExtRepository.class;
  }


  @Override
  protected ProjectionFactory getProjectionFactory(ClassLoader classLoader,
      BeanFactory beanFactory) {

    CollectionAwareProjectionFactory factory = new CollectionAwareProjectionFactory();
    factory.setBeanClassLoader(classLoader);
    factory.setBeanFactory(beanFactory);

    return factory;
  }


  @Override
  protected Optional<QueryLookupStrategy> getQueryLookupStrategy(Key key,
      QueryMethodEvaluationContextProvider evaluationContextProvider) {
    return Optional.of(
        JpaExtQueryLookupStrategy
            .create(entityManager, configuration, key, extractor, evaluationContextProvider,
                escapeCharacter,
                jpaExtProperties));
  }


  @Override
  @SuppressWarnings("unchecked")
  public <T, ID> JpaEntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
    return (JpaEntityInformation<T, ID>) JpaEntityInformationSupport.getEntityInformation(
        domainClass, entityManager);
  }

  @Override
  protected RepositoryComposition.RepositoryFragments getRepositoryFragments(
      RepositoryMetadata metadata) {
    RepositoryComposition.RepositoryFragments fragments = RepositoryComposition.RepositoryFragments
        .empty();

    boolean isQueryDslRepository = QUERY_DSL_PRESENT
        && QuerydslPredicateExecutor.class.isAssignableFrom(metadata.getRepositoryInterface());

    if (isQueryDslRepository) {

      if (metadata.isReactiveRepository()) {
        throw new InvalidDataAccessApiUsageException(
            "Cannot combine Querydsl and reactive repository support in a single interface");
      }

      JpaEntityInformation<?, Serializable> entityInformation = getEntityInformation(
          metadata.getDomainType());

      Object querydslFragment = getTargetRepositoryViaReflection(
          QuerydslJpaExtPredicateExecutor.class, jpaExtProperties,
          entityInformation, entityManager, entityPathResolver,
          crudMethodMetadataPostProcessor.getCrudMethodMetadata());

      fragments = fragments.append(RepositoryFragment.implemented(querydslFragment));
    }

    return fragments;
  }


  private static boolean hasMethodReturningStream(Class<?> repositoryClass) {

    Method[] methods = ReflectionUtils.getAllDeclaredMethods(repositoryClass);

    for (Method method : methods) {
      if (Stream.class.isAssignableFrom(method.getReturnType())) {
        return true;
      }
    }

    return false;
  }

  /**
   * Query creation listener that informs EclipseLink users that they have to be extra careful when
   * defining repository query methods using projections as we have to rely on the declaration order
   * of the accessors in projection interfaces matching the order in columns. Alias-based mapping
   * doesn't work with EclipseLink as it doesn't support {@link Tuple} based queries yet.
   *
   * @author Oliver Gierke
   * @since 2.0.5
   */
  private static class EclipseLinkProjectionQueryCreationListener implements
      QueryCreationListener<AbstractJpaQuery> {

    private final Logger log = LoggerFactory.getLogger(
        EclipseLinkProjectionQueryCreationListener.class);
    private static final String ECLIPSELINK_PROJECTIONS = "Usage of Spring Data projections detected on persistence provider EclipseLink. Make sure the following query methods declare result columns in exactly the order the accessors are declared in the projecting interface or the order of parameters for DTOs:";

    private final JpaMetamodel metamodel;

    private boolean warningLogged = false;

    /**
     * Creates a new EclipseLinkProjectionQueryCreationListener for the given {@link
     * EntityManager}.
     *
     * @param em must not be {@literal null}.
     */
    public EclipseLinkProjectionQueryCreationListener(EntityManager em) {

      Assert.notNull(em, "EntityManager must not be null!");

      this.metamodel = JpaMetamodel.of(em.getMetamodel());
    }

    @Override
    public void onCreation(AbstractJpaQuery query) {

      JpaQueryMethod queryMethod = query.getQueryMethod();
      ReturnedType type = queryMethod.getResultProcessor().getReturnedType();

      if (type.isProjecting() && !metamodel.isJpaManaged(type.getReturnedType())) {

        if (!warningLogged) {
          log.info(ECLIPSELINK_PROJECTIONS);
          this.warningLogged = true;
        }

        log.info(" - {}", queryMethod);
      }
    }
  }


}

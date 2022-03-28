package org.springframework.data.jpa.repository.support;

import static org.springframework.data.querydsl.QuerydslUtils.QUERY_DSL_PRESENT;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.jpa.projection.CollectionAwareProjectionFactory;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.provider.QueryExtractor;
import org.springframework.data.jpa.repository.query.AbstractJpaQuery;
import org.springframework.data.jpa.repository.query.DefaultJpaQueryMethodFactory;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.query.JpaExtQueryLookupStrategy;
import org.springframework.data.jpa.repository.query.JpaQueryMethod;
import org.springframework.data.jpa.repository.query.JpaQueryMethodFactory;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.jpa.util.JpaMetamodel;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.QueryCreationListener;
import org.springframework.data.repository.core.support.RepositoryComposition.RepositoryFragments;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.SurroundingTransactionDetectorMethodInterceptor;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.ReturnedType;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
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
  private JpaQueryMethodFactory queryMethodFactory;

  public JpaExtRepositoryFactory(EntityManager entityManager,
      Configuration configuration, JpaExtProperties jpaExtProperties) {
    Assert.notNull(entityManager, "EntityManager must not be null!");

    this.entityManager = entityManager;
    this.extractor = PersistenceProvider.fromEntityManager(entityManager);
    this.crudMethodMetadataPostProcessor = new CrudMethodMetadataPostProcessor();
    this.entityPathResolver = SimpleEntityPathResolver.INSTANCE;
    this.queryMethodFactory = new DefaultJpaQueryMethodFactory(extractor);

    addRepositoryProxyPostProcessor(crudMethodMetadataPostProcessor);
    addRepositoryProxyPostProcessor((factory, repositoryInformation) -> {

      if (isTransactionNeeded(repositoryInformation.getRepositoryInterface())) {
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

  public void setEntityPathResolver(EntityPathResolver entityPathResolver) {

    Assert.notNull(entityPathResolver, "EntityPathResolver must not be null!");

    this.entityPathResolver = entityPathResolver;
  }

  public void setEscapeCharacter(EscapeCharacter escapeCharacter) {
    this.escapeCharacter = escapeCharacter;
  }

  public void setQueryMethodFactory(JpaQueryMethodFactory queryMethodFactory) {

    Assert.notNull(queryMethodFactory, "QueryMethodFactory must not be null!");

    this.queryMethodFactory = queryMethodFactory;
  }


  @Override
  protected final JpaRepositoryImplementation<?, ?> getTargetRepository(
      RepositoryInformation information) {

    JpaRepositoryImplementation<?, ?> repository = getTargetRepository(information, entityManager);
    repository.setRepositoryMethodMetadata(crudMethodMetadataPostProcessor.getCrudMethodMetadata());
    repository.setEscapeCharacter(escapeCharacter);

    return repository;
  }

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
  protected RepositoryFragments getRepositoryFragments(RepositoryMetadata metadata) {

    return getRepositoryFragments(metadata, entityManager, entityPathResolver,
        crudMethodMetadataPostProcessor.getCrudMethodMetadata());
  }

  protected RepositoryFragments getRepositoryFragments(RepositoryMetadata metadata,
      EntityManager entityManager,
      EntityPathResolver resolver, CrudMethodMetadata crudMethodMetadata) {
    boolean isQueryDslRepository = QUERY_DSL_PRESENT
        && QuerydslPredicateExecutor.class.isAssignableFrom(metadata.getRepositoryInterface());

    if (isQueryDslRepository) {

      if (metadata.isReactiveRepository()) {
        throw new InvalidDataAccessApiUsageException(
            "Cannot combine Querydsl and reactive repository support in a single interface");
      }

      return RepositoryFragments.just(new QuerydslJpaExtPredicateExecutor<>(jpaExtProperties,
          getEntityInformation(metadata.getDomainType()),
          entityManager, resolver, crudMethodMetadata));
    }

    return RepositoryFragments.empty();
  }

  private static boolean isTransactionNeeded(Class<?> repositoryClass) {

    Method[] methods = ReflectionUtils.getAllDeclaredMethods(repositoryClass);

    for (Method method : methods) {
      if (Stream.class.isAssignableFrom(method.getReturnType()) || method.isAnnotationPresent(
          Procedure.class)) {
        return true;
      }
    }

    return false;
  }

  private static class EclipseLinkProjectionQueryCreationListener implements
      QueryCreationListener<AbstractJpaQuery> {

    private static final String ECLIPSELINK_PROJECTIONS = "Usage of Spring Data projections detected on persistence provider EclipseLink. Make sure the following query methods declare result columns in exactly the order the accessors are declared in the projecting interface or the order of parameters for DTOs:";

    private static final Logger log = org.slf4j.LoggerFactory
        .getLogger(EclipseLinkProjectionQueryCreationListener.class);

    private final JpaMetamodel metamodel;

    private boolean warningLogged = false;

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

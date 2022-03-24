package top.bettercode.simpleframework.data.jpa.config;

import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.cfg.AvailableSettings;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.orm.hibernate5.SpringBeanContainer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import top.bettercode.lang.util.ArrayUtil;

/**
 *
 */
public class MultiDatasourcesBeanDefinitionRegistryPostProcessor implements
    BeanDefinitionRegistryPostProcessor, ResourceLoaderAware, EnvironmentAware {

  private static final Logger logger = LoggerFactory.getLogger(
      MultiDatasourcesBeanDefinitionRegistryPostProcessor.class);

  private static final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

  private ResourceLoader resourceLoader;
  private Environment environment;


  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      throws BeansException {
    Map<String, BaseDataSourceProperties> dataSources = Binder.get(
        environment).bind("summer.datasource.multi.datasources", Bindable
        .mapOf(String.class, BaseDataSourceProperties.class)).orElse(null);
    if (dataSources != null) {
      DefaultListableBeanFactory factory = (DefaultListableBeanFactory) beanFactory;
      for (Entry<String, BaseDataSourceProperties> entry : dataSources.entrySet()) {
        //dataSource
        BaseDataSourceProperties properties = entry.getValue();
        String key = entry.getKey();
        boolean primary = "primary".equals(key);
        EnableJpaExtRepositories jpaExtRepositories = properties.getExtConfigClass()
            .getAnnotation(EnableJpaExtRepositories.class);
        String dataSourceBeanName = primary ? "dataSource" : key + "DataSource";
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(
            HikariDataSource.class, () -> {
              DataSourceProperties dataSourceProperties = new DataSourceProperties();
              dataSourceProperties.setUrl(properties.getUrl());
              dataSourceProperties.setUsername(properties.getUsername());
              dataSourceProperties.setPassword(properties.getPassword());

              HikariDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder()
                  .type(HikariDataSource.class).build();
              if (StringUtils.hasText(dataSourceProperties.getName())) {
                dataSource.setPoolName(dataSourceProperties.getName());
              }

              HikariDataSource hikari = properties.getHikari();
              String hikariConfigKey = "summer.datasource.multi.datasources." + key + ".hikari";
              if (hikari != null) {
                Binder.get(environment)
                    .bind(hikariConfigKey, Bindable.ofInstance(dataSource));
              } else {
                Binder.get(environment)
                    .bind("spring.datasource.hikari", Bindable.ofInstance(dataSource));
              }
              return dataSource;
            });
        if (primary) {
          beanDefinitionBuilder.setPrimary(primary);
          factory.removeBeanDefinition(dataSourceBeanName);
        }
        AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        beanDefinition.setSynthetic(true);
        beanDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
        factory.registerBeanDefinition(dataSourceBeanName, beanDefinition);

        //entityManagerFactory
        String entityManagerFactoryBeanName = jpaExtRepositories.entityManagerFactoryRef();
        beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(
            LocalContainerEntityManagerFactoryBean.class, () -> {

              EntityManagerFactoryBuilder builder = beanFactory.getBean(
                  EntityManagerFactoryBuilder.class);
              DataSource dataSource = beanFactory.getBean(dataSourceBeanName,
                  DataSource.class);
              HibernateProperties hibernateProperties = beanFactory.getBean(
                  HibernateProperties.class);
              JpaProperties jpaProperties = beanFactory.getBean(JpaProperties.class);
              List<String> mappingResourceList = jpaProperties.getMappingResources();
              String[] mappingResources = (!ObjectUtils.isEmpty(mappingResourceList)
                  ? StringUtils.toStringArray(mappingResourceList) : null);

              ObjectProvider<PhysicalNamingStrategy> physicalNamingStrategy = beanFactory.getBeanProvider(
                  PhysicalNamingStrategy.class);
              ObjectProvider<ImplicitNamingStrategy> implicitNamingStrategy = beanFactory.getBeanProvider(
                  ImplicitNamingStrategy.class);

              List<HibernatePropertiesCustomizer> hibernatePropertiesCustomizers = determineHibernatePropertiesCustomizers(
                  physicalNamingStrategy.getIfAvailable(), implicitNamingStrategy.getIfAvailable(),
                  beanFactory,
                  beanFactory.getBeanProvider(HibernatePropertiesCustomizer.class).orderedStream()
                      .collect(Collectors.toList()));

              LinkedHashMap<String, Object> vendorProperties = new LinkedHashMap<>(
                  hibernateProperties
                      .determineHibernateProperties(jpaProperties.getProperties(),
                          new HibernateSettings()
                              .hibernatePropertiesCustomizers(hibernatePropertiesCustomizers)
                      ));
              return builder
                  .dataSource(dataSource)
                  .properties(vendorProperties)
                  .packages(jpaExtRepositories.basePackages())
                  .mappingResources(mappingResources)
                  .build();
            });
        if (primary) {
          beanDefinitionBuilder.setPrimary(primary);
          factory.removeBeanDefinition(entityManagerFactoryBeanName);
        }
        beanDefinitionBuilder.addDependsOn(dataSourceBeanName);
        beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        beanDefinition.setSynthetic(true);
        beanDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
        factory.registerBeanDefinition(entityManagerFactoryBeanName, beanDefinition);

        //transactionManager
        String transactionManagerBeanName = jpaExtRepositories.transactionManagerRef();
        beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(
            PlatformTransactionManager.class, () -> {
              EntityManagerFactory entityManagerFactory = beanFactory.getBean(
                  entityManagerFactoryBeanName,
                  EntityManagerFactory.class);
              JpaTransactionManager jpaTransactionManager = new JpaTransactionManager(
                  entityManagerFactory);
              ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers = beanFactory.getBeanProvider(
                  TransactionManagerCustomizers.class);
              transactionManagerCustomizers.ifAvailable(
                  (customizers) -> customizers.customize(jpaTransactionManager));
              return jpaTransactionManager;
            });
        if (primary) {
          beanDefinitionBuilder.setPrimary(primary);
          factory.removeBeanDefinition(transactionManagerBeanName);
        }
        beanDefinitionBuilder.addDependsOn(entityManagerFactoryBeanName);
        beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        beanDefinition.setSynthetic(true);
        beanDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
        factory.registerBeanDefinition(transactionManagerBeanName, beanDefinition);

        //sqlSessionFactory
        String sqlSessionFactoryBeanName =
            primary ? "sqlSessionFactory" : key + "SqlSessionFactory";
        beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(
            SqlSessionFactory.class, () ->
                getSqlSessionFactory(
                    beanFactory.getBean(dataSourceBeanName, DataSource.class),
                    beanFactory.getBean(MybatisProperties.class),
                    resourceLoader,
                    properties
                ));
        if (primary) {
          beanDefinitionBuilder.setPrimary(primary);
          factory.removeBeanDefinition(sqlSessionFactoryBeanName);
        }
        beanDefinitionBuilder.addDependsOn(dataSourceBeanName);
        beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        beanDefinition.setSynthetic(true);
        beanDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
        factory.registerBeanDefinition(sqlSessionFactoryBeanName, beanDefinition);

        // sqlSessionTemplate
        String sqlSessionTemplateBeanName = jpaExtRepositories.sqlSessionTemplateRef();
        beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(
            SqlSessionTemplate.class, () -> {
              SqlSessionFactory sqlSessionFactory = beanFactory.getBean(sqlSessionFactoryBeanName,
                  SqlSessionFactory.class);
              ExecutorType executorType = beanFactory.getBean(MybatisProperties.class)
                  .getExecutorType();
              return executorType != null ? new SqlSessionTemplate(sqlSessionFactory, executorType)
                  : new SqlSessionTemplate(sqlSessionFactory);
            }
        );
        if (primary) {
          beanDefinitionBuilder.setPrimary(primary);
          factory.removeBeanDefinition(sqlSessionTemplateBeanName);
        }
        beanDefinitionBuilder.addDependsOn(sqlSessionFactoryBeanName);
        beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        beanDefinition.setSynthetic(true);
        beanDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
        factory.registerBeanDefinition(sqlSessionTemplateBeanName, beanDefinition);
      }
    }
  }

  private List<HibernatePropertiesCustomizer> determineHibernatePropertiesCustomizers(
      PhysicalNamingStrategy physicalNamingStrategy, ImplicitNamingStrategy implicitNamingStrategy,
      ConfigurableListableBeanFactory beanFactory,
      List<HibernatePropertiesCustomizer> hibernatePropertiesCustomizers) {
    List<HibernatePropertiesCustomizer> customizers = new ArrayList<>();
    if (ClassUtils.isPresent("org.hibernate.resource.beans.container.spi.BeanContainer",
        getClass().getClassLoader())) {
      customizers.add((properties) -> properties.put(AvailableSettings.BEAN_CONTAINER,
          new SpringBeanContainer(beanFactory)));
    }
    if (physicalNamingStrategy != null || implicitNamingStrategy != null) {
      customizers.add(
          new NamingStrategiesHibernatePropertiesCustomizer(physicalNamingStrategy,
              implicitNamingStrategy));
    }
    customizers.addAll(hibernatePropertiesCustomizers);
    return customizers;
  }

  private static class NamingStrategiesHibernatePropertiesCustomizer implements
      HibernatePropertiesCustomizer {

    private final PhysicalNamingStrategy physicalNamingStrategy;

    private final ImplicitNamingStrategy implicitNamingStrategy;

    NamingStrategiesHibernatePropertiesCustomizer(PhysicalNamingStrategy physicalNamingStrategy,
        ImplicitNamingStrategy implicitNamingStrategy) {
      this.physicalNamingStrategy = physicalNamingStrategy;
      this.implicitNamingStrategy = implicitNamingStrategy;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
      if (this.physicalNamingStrategy != null) {
        hibernateProperties.put("hibernate.physical_naming_strategy", this.physicalNamingStrategy);
      }
      if (this.implicitNamingStrategy != null) {
        hibernateProperties.put("hibernate.implicit_naming_strategy", this.implicitNamingStrategy);
      }
    }

  }


  private SqlSessionFactory getSqlSessionFactory(DataSource dataSource,
      MybatisProperties properties,
      ResourceLoader resourceLoader,
      BaseDataSourceProperties dataSourceProperties) {
    SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
    factory.setDataSource(dataSource);
    if (StringUtils.hasText(properties.getConfigLocation())) {
      factory
          .setConfigLocation(resourceLoader.getResource(properties.getConfigLocation()));
    }
    Configuration configuration = new Configuration();
    Configuration propertiesConfiguration = properties.getConfiguration();
    if (propertiesConfiguration != null) {
      BeanUtils.copyProperties(propertiesConfiguration, configuration);
    }
    factory.setConfiguration(configuration);
    if (properties.getConfigurationProperties() != null) {
      factory.setConfigurationProperties(properties.getConfigurationProperties());
    }

    if (StringUtils.hasLength(properties.getTypeAliasesPackage())) {
      factory.setTypeAliasesPackage(properties.getTypeAliasesPackage());
    }
    if (properties.getTypeAliasesSuperType() != null) {
      factory.setTypeAliasesSuperType(properties.getTypeAliasesSuperType());
    }
    if (StringUtils.hasLength(properties.getTypeHandlersPackage())) {
      factory.setTypeHandlersPackage(properties.getTypeHandlersPackage());
    }

    String[] mapperLocations = dataSourceProperties.getMapperLocations();
    if (ArrayUtil.isNotEmpty(mapperLocations)) {
      Resource[] resources = resolveMapperLocations(mapperLocations);
      if (!ObjectUtils.isEmpty(resources)) {
        factory.setMapperLocations(resources);
      }
    } else {
      Resource[] resources = properties.resolveMapperLocations();
      if (!ObjectUtils.isEmpty(resources)) {
        factory.setMapperLocations(resources);
      }
    }
    try {
      return factory.getObject();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Resource[] resolveMapperLocations(String[] mapperLocations) {
    return Stream.of(Optional.ofNullable(mapperLocations).orElse(new String[0]))
        .flatMap(location -> Stream.of(getResources(location))).toArray(Resource[]::new);
  }

  private Resource[] getResources(String location) {
    try {
      return resourceResolver.getResources(location);
    } catch (IOException e) {
      return new Resource[0];
    }
  }

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
      throws BeansException {

  }

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }
}
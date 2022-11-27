package top.bettercode.simpleframework.data.jpa.config;

import com.zaxxer.hikari.HikariDataSource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.apache.ibatis.session.Configuration;
import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.cfg.AvailableSettings;
import org.jetbrains.annotations.NotNull;
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
import org.springframework.core.io.ResourceLoader;
import org.springframework.orm.hibernate5.SpringBeanContainer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

public class MultiDatasourcesBeanDefinitionRegistryPostProcessor implements
    BeanDefinitionRegistryPostProcessor, ResourceLoaderAware, EnvironmentAware {

  private final Logger log = LoggerFactory.getLogger(
      MultiDatasourcesBeanDefinitionRegistryPostProcessor.class);
  private ResourceLoader resourceLoader;
  private Environment environment;

  @Override
  public void postProcessBeanFactory(@NotNull ConfigurableListableBeanFactory beanFactory)
      throws BeansException {
    Map<String, BaseDataSourceProperties> dataSources = Binder.get(
        environment).bind("summer.datasource.multi.datasources", Bindable
        .mapOf(String.class, BaseDataSourceProperties.class)).orElse(null);
    if (dataSources != null) {
      DefaultListableBeanFactory factory = (DefaultListableBeanFactory) beanFactory;
      for (Entry<String, BaseDataSourceProperties> entry : dataSources.entrySet()) {
        //dataSource
        BaseDataSourceProperties properties = entry.getValue();
        if ("false".equals(properties.getUrl())) {
          continue;
        }
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
              if (hikari != null) {
                String hikariConfigKey = "summer.datasource.multi.datasources." + key + ".hikari";
                Binder.get(environment)
                    .bind(hikariConfigKey, Bindable.ofInstance(dataSource));
              } else {
                Binder.get(environment)
                    .bind("spring.datasource.hikari", Bindable.ofInstance(dataSource));
              }
              if (!StringUtils.hasText(dataSource.getPoolName())) {
                dataSource.setPoolName(key + "Pool");
              }
              if (log.isInfoEnabled()) {
                log.info("init dataSource {} : {}", dataSource.getPoolName(),
                    dataSource.getJdbcUrl());
              }
              return dataSource;
            });
        if (primary) {
          beanDefinitionBuilder.setPrimary(true);
          if (factory.containsBeanDefinition(dataSourceBeanName)) {
            factory.removeBeanDefinition(dataSourceBeanName);
          }
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
          beanDefinitionBuilder.setPrimary(true);
          if (factory.containsBeanDefinition(entityManagerFactoryBeanName)) {
            factory.removeBeanDefinition(entityManagerFactoryBeanName);
          }
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
          beanDefinitionBuilder.setPrimary(true);
          if (factory.containsBeanDefinition(transactionManagerBeanName)) {
            factory.removeBeanDefinition(transactionManagerBeanName);
          }
        }
        beanDefinitionBuilder.addDependsOn(entityManagerFactoryBeanName);
        beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        beanDefinition.setSynthetic(true);
        beanDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
        factory.registerBeanDefinition(transactionManagerBeanName, beanDefinition);

        // mybatisConfiguration
        String mybatisConfigurationRef = jpaExtRepositories.mybatisConfigurationRef();
        beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(
            Configuration.class, () -> {
              try {
                MybatisProperties mybatisProperties = beanFactory.getBean(MybatisProperties.class);
                Configuration configuration = mybatisProperties.getConfiguration();
                if (configuration != null) {
                  Configuration newConfiguration = new Configuration();
                  BeanUtils.copyProperties(configuration, newConfiguration);
                  configuration = newConfiguration;
                }
                return JpaMybatisAutoConfiguration.mybatisConfiguration(configuration,
                    mybatisProperties, resourceLoader, properties.getMapperLocations());
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            }
        );
        if (primary) {
          beanDefinitionBuilder.setPrimary(true);
          if (factory.containsBeanDefinition(mybatisConfigurationRef)) {
            factory.removeBeanDefinition(mybatisConfigurationRef);
          }
        }
        beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        beanDefinition.setSynthetic(true);
        beanDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
        factory.registerBeanDefinition(mybatisConfigurationRef, beanDefinition);
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


  @Override
  public void postProcessBeanDefinitionRegistry(@NotNull BeanDefinitionRegistry registry)
      throws BeansException {
  }

  @Override
  public void setResourceLoader(@NotNull ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @Override
  public void setEnvironment(@NotNull Environment environment) {
    this.environment = environment;
  }
}
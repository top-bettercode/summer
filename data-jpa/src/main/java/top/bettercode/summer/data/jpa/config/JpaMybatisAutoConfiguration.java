package top.bettercode.summer.data.jpa.config;

import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import top.bettercode.summer.data.jpa.BaseRepository;
import top.bettercode.summer.data.jpa.JpaExtRepository;
import top.bettercode.summer.data.jpa.querydsl.QuerydslRepository;
import top.bettercode.summer.data.jpa.support.SimpleJpaExtRepository;
import top.bettercode.summer.tools.lang.util.ArrayUtil;
import top.bettercode.summer.web.support.ApplicationContextHolder;
import top.bettercode.summer.web.support.packagescan.PackageScanClassResolver;

/**
 * {@link EnableAutoConfiguration Auto-Configuration} for Mybatis.
 */
@org.springframework.context.annotation.Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(MultiDatasourcesBeanDefinitionRegistryPostProcessor.class)
public class JpaMybatisAutoConfiguration implements InitializingBean {

  private final static Logger log = LoggerFactory.getLogger(JpaMybatisAutoConfiguration.class);

  private static final ResourcePatternResolver RESOURCE_PATTERN_RESOLVER = new PathMatchingResourcePatternResolver();
  private static final MetadataReaderFactory METADATA_READER_FACTORY = new CachingMetadataReaderFactory();
  private static final PackageScanClassResolver PACKAGE_SCAN_CLASS_RESOLVER = new PackageScanClassResolver();
  private final MybatisProperties properties;
  private final ResourceLoader resourceLoader;

  public JpaMybatisAutoConfiguration(
      MybatisProperties properties,
      ResourceLoader resourceLoader, @Autowired(required = false) HikariDataSource hikari) {
    this.properties = properties;
    this.resourceLoader = resourceLoader;
    if (hikari != null && log.isInfoEnabled()) {
      log.info("init dataSource {} : {}", hikari.getPoolName(), hikari.getJdbcUrl());
    }
  }

  @Override
  public void afterPropertiesSet() {
    checkConfigFileExists();
  }

  private void checkConfigFileExists() {
    if (this.properties.isCheckConfigLocation() && StringUtils
        .hasText(this.properties.getConfigLocation())) {
      Resource resource = this.resourceLoader.getResource(this.properties.getConfigLocation());
      Assert.state(resource.exists(),
          "Cannot find config location: " + resource
              + " (please add config file or check your Mybatis configuration)");
    }
  }

  @Bean
  public Configuration mybatisConfiguration() throws Exception {
    return mybatisConfiguration(properties.getConfiguration(), this.properties, this.resourceLoader,
        null);
  }

  public static Set<String> findDefaultMapperLocations(GenericApplicationContext applicationContext)
      throws ClassNotFoundException {
    Set<String> packages = new HashSet<>();
    String[] beanNames = applicationContext.getBeanNamesForAnnotation(SpringBootApplication.class);
    for (String beanName : beanNames) {
      AbstractBeanDefinition beanDefinition = (AbstractBeanDefinition) applicationContext.getBeanDefinition(
          beanName);
      if (!beanDefinition.hasBeanClass()) {
        beanDefinition.resolveBeanClass(JpaMybatisAutoConfiguration.class.getClassLoader());
      }
      Class<?> beanClass = beanDefinition.getBeanClass();
      SpringBootApplication annotation = AnnotatedElementUtils.findMergedAnnotation(beanClass,
          SpringBootApplication.class);
      for (Class<?> packageClass : Objects.requireNonNull(annotation).scanBasePackageClasses()) {
        packages.add(packageClass.getPackage().getName());
      }
      packages.addAll(Arrays.asList(annotation.scanBasePackages()));
      packages.add(beanClass.getPackage().getName());
    }
    Set<Class<?>> implementations = PACKAGE_SCAN_CLASS_RESOLVER.findImplementations(
        JpaExtRepository.class, packages.toArray(new String[0]));
    Set<String> excludeMapperLocations = new HashSet<>();
    excludeMapperLocations.add(BaseRepository.class.getPackage().getName());
    excludeMapperLocations.add(QuerydslRepository.class.getPackage().getName());
    excludeMapperLocations.add(SimpleJpaExtRepository.class.getPackage().getName());
    Set<String> mapperLocations = new HashSet<>();
//    classpath*:/@app.packagePath@/modules/*/*/*.xml
    for (Class<?> implementation : implementations) {
      String name = implementation.getPackage().getName();
      if (!excludeMapperLocations.contains(name)) {
        mapperLocations.add(
            "classpath*:/" + name.replace(".", "/") + "/*.xml");
      }
    }

    return mapperLocations;
  }

  public static Configuration mybatisConfiguration(Configuration configuration,
      MybatisProperties properties,
      ResourceLoader resourceLoader, String[] mapperLocations) throws Exception {
    Properties configurationProperties = properties.getConfigurationProperties();

    XMLConfigBuilder xmlConfigBuilder = null;
    Resource configResource = null;
    if (configuration == null) {
      String configLocation = properties.getConfigLocation();
      if (StringUtils.hasText(configLocation)) {
        configResource = resourceLoader.getResource(configLocation);
        xmlConfigBuilder = new XMLConfigBuilder(configResource.getInputStream(), null,
            configurationProperties);
        configuration = xmlConfigBuilder.getConfiguration();
      } else {
        if (log.isDebugEnabled()) {
          log.debug(
              "Property 'configuration' or 'configLocation' not specified, using default MyBatis Configuration");
        }
        configuration = new Configuration();
        Optional.ofNullable(configurationProperties).ifPresent(configuration::setVariables);
      }
    }
    String typeAliasesPackage = properties.getTypeAliasesPackage();
    if (StringUtils.hasText(typeAliasesPackage)) {
      scanClasses(typeAliasesPackage, properties.getTypeAliasesSuperType()).stream()
          .filter(clazz -> !clazz.isAnonymousClass()).filter(clazz -> !clazz.isInterface())
          .filter(clazz -> !clazz.isMemberClass())
          .forEach(configuration.getTypeAliasRegistry()::registerAlias);
    }
    Class<?>[] typeAliases = properties.getTypeAliases();
    Configuration finalConfiguration = configuration;
    if (ArrayUtil.isNotEmpty(typeAliases)) {
      Stream.of(typeAliases).forEach(typeAlias -> {
        finalConfiguration.getTypeAliasRegistry().registerAlias(typeAlias);
        if (log.isTraceEnabled()) {
          log.trace("Registered type alias: '" + typeAlias + "'");
        }
      });
    }
    String typeHandlersPackage = properties.getTypeHandlersPackage();
    if (StringUtils.hasText(typeHandlersPackage)) {
      scanClasses(typeHandlersPackage, TypeHandler.class).stream()
          .filter(clazz -> !clazz.isAnonymousClass())
          .filter(clazz -> !clazz.isInterface())
          .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
          .forEach(configuration.getTypeHandlerRegistry()::register);
    }

    Class<TypeHandler<?>>[] typeHandlerClasses = properties.getTypeHandlerClasses();
    if (!ArrayUtil.isEmpty(typeHandlerClasses)) {
      Stream.of(typeHandlerClasses).forEach(typeHandler -> {
        finalConfiguration.getTypeHandlerRegistry().register(typeHandler);
        if (log.isTraceEnabled()) {
          log.trace("Registered type handler: '" + typeHandler + "'");
        }
      });
    }

    if (xmlConfigBuilder != null) {
      try {
        xmlConfigBuilder.parse();
        if (log.isTraceEnabled()) {
          log.trace("Parsed configuration file: '" + configResource + "'");
        }
      } catch (Exception ex) {
        throw new IOException("Failed to parse config resource: " + configResource, ex);
      } finally {
        ErrorContext.instance().reset();
      }
    }

    if (ArrayUtil.isEmpty(mapperLocations)) {
      mapperLocations = properties.getMapperLocations();
    }

    if (ArrayUtil.isEmpty(mapperLocations)) {
      GenericApplicationContext applicationContext = (GenericApplicationContext) ApplicationContextHolder.getApplicationContext();
      Set<String> defaultMapperLocations = JpaMybatisAutoConfiguration.findDefaultMapperLocations(
          applicationContext);
      mapperLocations = defaultMapperLocations.toArray(new String[0]);
    }

    Resource[] mapperResources = resolveMapperLocations(mapperLocations);

    if (configuration.getVariables() == null) {
      configuration.setVariables(configurationProperties);
    } else if (configurationProperties != null) {
      configuration.getVariables().putAll(configurationProperties);
    }

    if (mapperResources.length == 0) {
      if (log.isInfoEnabled()) {
        log.info(
            "Property 'mapperLocations' was specified but matching resources are not found.");
      }
    } else {
      for (Resource mapperResource : mapperResources) {
        if (mapperResource == null) {
          continue;
        }
        try {
          XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(
              mapperResource.getInputStream(),
              configuration, mapperResource.toString(), configuration.getSqlFragments());
          xmlMapperBuilder.parse();
        } catch (Exception e) {
          throw new IOException(
              "Failed to parse mapping resource: '" + mapperResource + "'", e);
        } finally {
          ErrorContext.instance().reset();
        }
        if (log.isTraceEnabled()) {
          log.trace("Parsed mapper file: '" + mapperResource + "'");
        }
      }
    }

    return configuration;
  }

  public static Resource[] resolveMapperLocations(String[] mapperLocations) {
    return Stream.of(
            Optional.ofNullable(mapperLocations).orElse(new String[0]))
        .flatMap(location -> Stream.of(getResources(location))).toArray(Resource[]::new);
  }

  private static Resource[] getResources(String location) {
    try {
      return RESOURCE_PATTERN_RESOLVER.getResources(location);
    } catch (IOException e) {
      return new Resource[0];
    }
  }

  private static Set<Class<?>> scanClasses(String packagePatterns, Class<?> assignableType)
      throws IOException {
    Set<Class<?>> classes = new HashSet<>();
    String[] packagePatternArray = StringUtils.tokenizeToStringArray(packagePatterns,
        ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
    for (String packagePattern : packagePatternArray) {
      Resource[] resources = RESOURCE_PATTERN_RESOLVER.getResources(
          ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
              + ClassUtils.convertClassNameToResourcePath(packagePattern) + "/**/*.class");
      for (Resource resource : resources) {
        try {
          ClassMetadata classMetadata = METADATA_READER_FACTORY.getMetadataReader(resource)
              .getClassMetadata();
          Class<?> clazz = Resources.classForName(classMetadata.getClassName());
          if (assignableType == null || assignableType.isAssignableFrom(clazz)) {
            classes.add(clazz);
          }
        } catch (Throwable e) {
          if (log.isWarnEnabled()) {
            log.warn("Cannot load the '" + resource + "'. Cause by " + e);
          }
        }
      }
    }
    return classes;
  }
}

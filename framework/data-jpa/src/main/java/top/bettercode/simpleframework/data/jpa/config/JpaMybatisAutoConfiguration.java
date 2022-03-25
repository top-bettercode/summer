package top.bettercode.simpleframework.data.jpa.config;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashSet;
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
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.NestedIOException;
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
import top.bettercode.lang.util.ArrayUtil;

/**
 * {@link EnableAutoConfiguration Auto-Configuration} for Mybatis.
 */
@org.springframework.context.annotation.Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({MybatisProperties.class})
public class JpaMybatisAutoConfiguration implements InitializingBean {

  private final static Logger log = LoggerFactory.getLogger(JpaMybatisAutoConfiguration.class);

  private static final ResourcePatternResolver RESOURCE_PATTERN_RESOLVER = new PathMatchingResourcePatternResolver();
  private static final MetadataReaderFactory METADATA_READER_FACTORY = new CachingMetadataReaderFactory();

  private final MybatisProperties properties;
  private final ResourceLoader resourceLoader;

  public JpaMybatisAutoConfiguration(
      MybatisProperties properties,
      ResourceLoader resourceLoader) {
    this.properties = properties;
    this.resourceLoader = resourceLoader;
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
  @ConditionalOnMissingBean
  public Configuration mybatisConfiguration() throws Exception {
    return mybatisConfiguration(this.properties, this.resourceLoader, null);
  }

  public static Configuration mybatisConfiguration(MybatisProperties properties,
      ResourceLoader resourceLoader, String[] mapperLocations) throws Exception {
    Properties configurationProperties = null;
    if (properties.getConfigurationProperties() != null) {
      configurationProperties = (properties.getConfigurationProperties());
    }
    Configuration configuration = properties.getConfiguration();
    XMLConfigBuilder xmlConfigBuilder = null;
    Resource configLocation = null;
    if (configuration == null) {
      if (StringUtils.hasText(properties.getConfigLocation())) {
        configLocation = resourceLoader.getResource(
            properties.getConfigLocation());
        xmlConfigBuilder = new XMLConfigBuilder(configLocation.getInputStream(),
            null, configurationProperties);
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
        if (log.isDebugEnabled()) {
          log.debug("Registered type alias: '" + typeAlias + "'");
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

    TypeHandler<?>[] typeHandlers = properties.getTypeHandlers();
    if (!ArrayUtil.isEmpty(typeHandlers)) {
      Stream.of(typeHandlers).forEach(typeHandler -> {
        finalConfiguration.getTypeHandlerRegistry().register(typeHandler);
        if (log.isDebugEnabled()) {
          log.debug("Registered type handler: '" + typeHandler + "'");
        }
      });
    }

    if (xmlConfigBuilder != null) {
      try {
        xmlConfigBuilder.parse();
        if (log.isDebugEnabled()) {
          log.debug("Parsed configuration file: '" + configLocation + "'");
        }
      } catch (Exception ex) {
        throw new NestedIOException("Failed to parse config resource: " + configLocation, ex);
      } finally {
        ErrorContext.instance().reset();
      }
    }

    Resource[] mapperResources;
    if (ArrayUtil.isNotEmpty(mapperLocations)) {
      mapperResources = Stream.of(Optional.ofNullable(mapperLocations).orElse(new String[0]))
          .flatMap(location -> Stream.of(getResources(location))).toArray(Resource[]::new);
    } else {
      mapperResources = properties.resolveMapperLocations();
    }

    if (configuration.getVariables() == null) {
      configuration.setVariables(configurationProperties);
    } else if (configurationProperties != null) {
      configuration.getVariables().putAll(configurationProperties);
    }

    if (mapperResources != null) {
      if (mapperResources.length == 0) {
        if (log.isWarnEnabled()) {
          log.warn(
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
            throw new NestedIOException(
                "Failed to parse mapping resource: '" + mapperResource + "'", e);
          } finally {
            ErrorContext.instance().reset();
          }
          if (log.isDebugEnabled()) {
            log.debug("Parsed mapper file: '" + mapperResource + "'");
          }
        }
      }
    } else {
      if (log.isDebugEnabled()) {
        log.debug("Property 'mapperLocations' was not specified.");
      }
    }

    return configuration;
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
            log.warn("Cannot load the '" + resource + "'. Cause by " + e.toString());
          }
        }
      }
    }
    return classes;
  }
}

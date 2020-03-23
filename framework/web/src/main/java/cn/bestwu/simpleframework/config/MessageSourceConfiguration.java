package cn.bestwu.simpleframework.config;

import java.time.Duration;
import java.util.Set;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.context.MessageSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnMissingBean(value = MessageSource.class, search = SearchStrategy.CURRENT)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@AutoConfigureBefore(MessageSourceAutoConfiguration.class)
@EnableConfigurationProperties
public class MessageSourceConfiguration {

  public static final String BASE_MESSAGES = "base-messages";
  public static final String CORE_MESSAGES = "core-messages";
  private static final Resource[] NO_RESOURCES = {};

  @Bean
  @ConfigurationProperties(prefix = "spring.messages")
  public MessageSourceProperties messageSourceProperties() {
    return new MessageSourceProperties();
  }

  @Bean
  public MessageSource messageSource(ApplicationContext applicationContext,
      MessageSourceProperties messageSourceProperties) {
    String basename = messageSourceProperties.getBasename();
    Set<String> messageNames = StringUtils.commaDelimitedListToSet(
        StringUtils.trimAllWhitespace(basename));
    String defaultMessagesName = "messages";
    if (messageNames.contains(defaultMessagesName) && ResourceBundleCondition
        .getResources(applicationContext.getClassLoader(), defaultMessagesName).length == 0) {
      messageNames.remove(defaultMessagesName);
    }
    messageNames.add(BASE_MESSAGES);
    if (!messageNames.contains(CORE_MESSAGES)) {
      Resource[] resources = ResourceBundleCondition
          .getResources(applicationContext.getClassLoader(), CORE_MESSAGES);
      if (resources.length > 0 && resources[0].exists()) {
        messageNames.add(CORE_MESSAGES);
      }
    }
    basename = StringUtils.collectionToCommaDelimitedString(messageNames);
    messageSourceProperties.setBasename(basename);

    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    if (!messageNames.isEmpty()) {
      messageSource.setBasenames(messageNames.toArray(new String[0]));
    }
    if (messageSourceProperties.getEncoding() != null) {
      messageSource.setDefaultEncoding(messageSourceProperties.getEncoding().name());
    }
    messageSource.setFallbackToSystemLocale(messageSourceProperties.isFallbackToSystemLocale());
    Duration cacheDuration = messageSourceProperties.getCacheDuration();
    if (cacheDuration != null) {
      messageSource.setCacheMillis(cacheDuration.toMillis());
    }
    messageSource.setAlwaysUseMessageFormat(messageSourceProperties.isAlwaysUseMessageFormat());
    messageSource.setUseCodeAsDefaultMessage(messageSourceProperties.isUseCodeAsDefaultMessage());
    return messageSource;
  }

  protected static class ResourceBundleCondition extends SpringBootCondition {

    private static ConcurrentReferenceHashMap<String, ConditionOutcome> cache = new ConcurrentReferenceHashMap<>();

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context,
        AnnotatedTypeMetadata metadata) {
      String basename = context.getEnvironment()
          .getProperty("spring.messages.basename", "messages");
      ConditionOutcome outcome = cache.get(basename);
      if (outcome == null) {
        outcome = getMatchOutcomeForBasename(context, basename);
        cache.put(basename, outcome);
      }
      return outcome;
    }

    private ConditionOutcome getMatchOutcomeForBasename(ConditionContext context,
        String basename) {
      ConditionMessage.Builder message = ConditionMessage
          .forCondition("ResourceBundle");
      for (String name : StringUtils.commaDelimitedListToStringArray(
          StringUtils.trimAllWhitespace(basename))) {
        for (Resource resource : getResources(context.getClassLoader(), name)) {
          if (resource.exists()) {
            return ConditionOutcome
                .match(message.found("bundle").items(resource));
          }
        }
      }
      return ConditionOutcome.noMatch(
          message.didNotFind("bundle with basename " + basename).atAll());
    }

    private static Resource[] getResources(ClassLoader classLoader, String name) {
      String target = name.replace('.', '/');
      try {
        return new PathMatchingResourcePatternResolver(classLoader)
            .getResources("classpath*:" + target + ".properties");
      } catch (Exception ex) {
        return NO_RESOURCES;
      }
    }

  }
}

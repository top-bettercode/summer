package top.bettercode.simpleframework.data.jpa.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.data.jpa.repository.config.JpaRepositoryConfigExtension;
import org.springframework.data.repository.config.RepositoryConfigurationSource;

/**
 * @author Peter Wu
 */
public class JpaExtRepositoryConfigExtension extends JpaRepositoryConfigExtension {

  @Override
  public void postProcess(
      @NotNull BeanDefinitionBuilder builder, @NotNull RepositoryConfigurationSource source) {
    super.postProcess(builder, source);
    builder.addPropertyReference("mybatisConfiguration", source.getAttribute("mybatisConfigurationRef").orElse("mybatisConfiguration"));
    builder.addPropertyReference("jpaExtProperties", source.getAttribute("jpaExtPropertiesRef").orElse("jpaExtProperties"));
  }
}

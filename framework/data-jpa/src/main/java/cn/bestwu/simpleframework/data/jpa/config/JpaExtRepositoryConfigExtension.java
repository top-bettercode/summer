package cn.bestwu.simpleframework.data.jpa.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.data.jpa.repository.config.JpaRepositoryConfigExtension;
import org.springframework.data.repository.config.RepositoryConfigurationSource;

/**
 * @author Peter Wu
 */
public class JpaExtRepositoryConfigExtension extends JpaRepositoryConfigExtension {

  @Override
  public void postProcess(BeanDefinitionBuilder builder, RepositoryConfigurationSource source) {
    super.postProcess(builder, source);
    builder.addPropertyReference("sqlSessionTemplate", source.getAttribute("sqlSessionTemplateRef").orElse("sqlSessionTemplate"));
    builder.addPropertyReference("jpaExtProperties", source.getAttribute("jpaExtPropertiesRef").orElse("jpaExtProperties"));
  }
}

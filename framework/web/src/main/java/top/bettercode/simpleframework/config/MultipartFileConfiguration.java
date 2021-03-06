package top.bettercode.simpleframework.config;

import java.io.File;
import javax.servlet.MultipartConfigElement;
import kotlin.collections.ArraysKt;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.WebProperties.Resources;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.bettercode.simpleframework.web.resolver.multipart.MuipartFileToAttachmentConverter;

/**
 * 文件上传 配置
 *
 * @author Peter Wu
 */
@ConditionalOnProperty(prefix = "summer.multipart", value = "base-save-path")
@EnableConfigurationProperties({MultipartProperties.class,
    org.springframework.boot.autoconfigure.web.servlet.MultipartProperties.class})
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@AutoConfigureBefore(MultipartAutoConfiguration.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MultipartFileConfiguration implements WebMvcConfigurer {


  public MultipartFileConfiguration(
      MultipartProperties multipartProperties,
      WebProperties webProperties) {

    Resources resources = webProperties.getResources();
    resources.setStaticLocations(ArraysKt.plus(resources.getStaticLocations(),
        multipartProperties.getStaticLocations()));
  }

  @Bean
  public MultipartConfigElement multipartConfigElement(
      org.springframework.boot.autoconfigure.web.servlet.MultipartProperties multipartProperties,
      MultipartProperties properties) {
    File file = new File(properties.getBaseSavePath(), "tmp").getAbsoluteFile();
    multipartProperties.setLocation(file.getAbsolutePath());
    file.mkdirs();
    return multipartProperties.createMultipartConfig();
  }

  @Bean
  public MuipartFileToAttachmentConverter muipartFileToAttachmentConverter(
      MultipartProperties multipartProperties) {
    return new MuipartFileToAttachmentConverter(multipartProperties);
  }
}

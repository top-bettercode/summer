package cn.bestwu.simpleframework.config;

import cn.bestwu.logging.annotation.NoRequestLogging;
import cn.bestwu.simpleframework.support.packagescan.PackageScanClassResolver;
import cn.bestwu.simpleframework.web.CustomErrorController;
import cn.bestwu.simpleframework.web.DataErrorHandler;
import cn.bestwu.simpleframework.web.DefaultCaptchaServiceImpl;
import cn.bestwu.simpleframework.web.ErrorAttributes;
import cn.bestwu.simpleframework.web.ICaptchaService;
import cn.bestwu.simpleframework.web.IErrorHandler;
import cn.bestwu.simpleframework.web.filter.ApiVersionFilter;
import cn.bestwu.simpleframework.web.filter.OrderedHiddenHttpMethodFilter;
import cn.bestwu.simpleframework.web.filter.OrderedHttpPutFormContentFilter;
import cn.bestwu.simpleframework.web.kaptcha.KaptchaProperties;
import cn.bestwu.simpleframework.web.resolver.StringToEnumConverterFactory;
import cn.bestwu.simpleframework.web.resolver.WrappProcessorInvokingHandlerAdapter;
import cn.bestwu.simpleframework.web.serializer.BigDecimalSerializer;
import cn.bestwu.simpleframework.web.serializer.MixIn;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * Rest MVC 配置
 *
 * @author Peter Wu
 */
@Configuration
@ConditionalOnWebApplication
@AutoConfigureBefore({ErrorMvcAutoConfiguration.class, JacksonAutoConfiguration.class})
public class FrameworkMvcConfiguration {

  private final Logger log = LoggerFactory.getLogger(FrameworkMvcConfiguration.class);

  /**
   * specifying the packages to scan for mixIn annotation.
   */
  @Value("${app.jackson.mix-in-annotation.base-packages:}")
  private String[] basePackages;

  @Value("${app.web.ok.enable:false}")
  private Boolean okEnable;

  @Bean(name = "error")
  @ConditionalOnMissingBean(name = "error")
  public View error(ObjectMapper objectMapper,
      @Value("${app.web.ok.enable:false}") Boolean okEnable) {
    return new View() {
      @Override
      public String getContentType() {
        return "text/html;charset=utf-8";
      }

      @Override
      public void render(Map<String, ?> model, HttpServletRequest request,
          HttpServletResponse response)
          throws Exception {
        if (response.getContentType() == null) {
          response.setContentType(getContentType());
        }
        String result = objectMapper.writeValueAsString(model);
        if (okEnable) {
          response.setStatus(HttpStatus.OK.value());
        }
        response.getWriter().append(result);
      }
    };
  }

  @Bean
  public Module module(ApplicationContext applicationContext,
      PackageScanClassResolver packageScanClassResolver) {
    SimpleModule module = new SimpleModule();
    Set<String> packages = PackageScanClassResolver
        .detectPackagesToScan(applicationContext, basePackages);

    packages.add("cn.bestwu.simpleframework.data.serializer");

    Set<Class<?>> allSubClasses = packageScanClassResolver
        .findImplementations(MixIn.class, packages.toArray(new String[0]));
    for (Class<?> aClass : allSubClasses) {
      try {
        ParameterizedType object = (ParameterizedType) aClass.getGenericInterfaces()[0];
        Class targetType = (Class) object.getActualTypeArguments()[0];
        if (log.isDebugEnabled()) {
          log.debug("Detected MixInAnnotation:{}=>{}", targetType, aClass);
        }
        module.setMixInAnnotation(targetType, aClass);
      } catch (Exception e) {
        log.warn(aClass + "Detected fail", e);
      }
    }
    return module;
  }


  /*
   * 响应增加api version
   */
  @Bean
  public ApiVersionFilter apiVersionFilter(
      @Value("${app.version-name:apiVersion}") String appVersionName,
      @Value("${app.version:v1.0}") String appVersion,
      @Value("${app.version-no-name:apiVersionNo}") String appVersionNoName,
      @Value("${app.version-no:1}") String appVersionNo) {
    return new ApiVersionFilter(appVersionName, appVersion, appVersionNoName, appVersionNo);
  }

  /*
   * 隐藏方法，网页支持
   */
  @Bean
  public OrderedHiddenHttpMethodFilter hiddenHttpMethodFilter() {
    return new OrderedHiddenHttpMethodFilter();
  }

  /*
   * Put方法，网页支持
   */
  @Bean
  public OrderedHttpPutFormContentFilter putFormContentFilter() {
    return new OrderedHttpPutFormContentFilter();
  }

  @ConditionalOnMissingBean(ErrorAttributes.class)
  @Bean
  public ErrorAttributes errorAttributes(
      @Autowired(required = false) List<IErrorHandler> errorHandlers) {
    return new ErrorAttributes(errorHandlers);
  }

  @ConditionalOnMissingBean(ErrorController.class)
  @Bean
  public CustomErrorController customErrorController(ErrorAttributes errorAttributes,
      ServerProperties serverProperties,
      @Autowired(required = false) @Qualifier("corsConfigurationSource") CorsConfigurationSource configSource) {
    return new CustomErrorController(errorAttributes, serverProperties.getError(), configSource,
        okEnable);
  }

  @Configuration
  @ConditionalOnClass(org.springframework.jdbc.UncategorizedSQLException.class)
  @ConditionalOnWebApplication
  protected static class ErrorHandlerConfiguration {

    @Bean
    public DataErrorHandler dataErrorHandler() {
      return new DataErrorHandler();
    }

  }

  @Configuration
  @ConditionalOnWebApplication
  protected static class ObjectMapperBuilderCustomizer implements
      Jackson2ObjectMapperBuilderCustomizer {

    @Override
    public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
      jacksonObjectMapperBuilder.serializerByType(BigDecimal.class, new BigDecimalSerializer());
    }
  }

  @Configuration
  @ConditionalOnClass(DefaultKaptcha.class)
  @ConditionalOnWebApplication
  @EnableConfigurationProperties(KaptchaProperties.class)
  protected static class KaptchaConfiguration {

    @Bean
    @ConditionalOnMissingBean(Producer.class)
    public DefaultKaptcha kaptcha(KaptchaProperties kaptchaProperties) {
      Properties properties = new Properties();
      properties.put("kaptcha.border", kaptchaProperties.getBorder());
      properties
          .put("kaptcha.textproducer.font.color", kaptchaProperties.getTextproducerFontColor());
      properties
          .put("kaptcha.textproducer.char.space", kaptchaProperties.getTextproducerCharSpace());
      Config config = new Config(properties);
      DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
      defaultKaptcha.setConfig(config);
      return defaultKaptcha;
    }

    @Bean
    @ConditionalOnMissingBean(ICaptchaService.class)
    public ICaptchaService captchaService(@Autowired(required = false) HttpSession httpSession,
        KaptchaProperties kaptchaProperties) {
      return new DefaultCaptchaServiceImpl(httpSession, kaptchaProperties);
    }

    @Controller
    protected static class CaptchaController {

      private Logger log = LoggerFactory.getLogger(CaptchaController.class);
      private final Producer producer;
      private final ICaptchaService captchaService;

      public CaptchaController(Producer producer,
          ICaptchaService captchaService) {
        this.producer = producer;
        this.captchaService = captchaService;
      }

      @NoRequestLogging
      @GetMapping(value = "/captcha.jpg", name = "图片验证码")
      public void captcha(HttpServletRequest request, HttpServletResponse response, String loginId)
          throws IOException {

        //生成文字验证码
        String text = producer.createText();
        if (log.isDebugEnabled()) {
          log.debug("验证码：{}", text);
        }
        //生成图片验证码
        BufferedImage image = producer.createImage(text);
        if (!StringUtils.hasText(loginId)) {
          loginId = request.getRequestedSessionId();
        }
        captchaService.save(loginId, text);

        response.setContentType("image/jpeg");
        response.addHeader("loginId", loginId);
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "jpg", out);
      }
    }
  }

  @Conditional(CustomRequestMappingConditio.class)
  @Bean
  public WebMvcRegistrations webMvcRegistrations(
      @Value("${app.web.ok.enable:false}") Boolean okEnable,
      @Value("${app.web.wrap.enable:false}") Boolean wrapEnable) {
    return new WebMvcRegistrations() {
      @Override
      public RequestMappingHandlerAdapter getRequestMappingHandlerAdapter() {
        return new WrappProcessorInvokingHandlerAdapter(okEnable, wrapEnable);
      }

    };
  }

  public static class CustomRequestMappingConditio implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
      return "true".equals(context.getEnvironment().getProperty("app.web.wrap.enable")) || "true"
          .equals(context.getEnvironment().getProperty("app.web.ok.enable"));
    }
  }

  @Configuration
  @ConditionalOnWebApplication
  protected static class WebMvcConfiguration implements WebMvcConfigurer {

    /**
     * @param registry 注册转换类
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
      registry.addConverterFactory(new StringToEnumConverterFactory());
      registry.addConverter(new Converter<String, Date>() {
        @Override
        public Date convert(String source) {
          if (StringUtils.hasLength(source) && !"null".equals(source)) {
            return new Date(Long.parseLong(source));
          } else {
            return null;
          }
        }
      });
      registry.addConverter(new Converter<String, java.sql.Date>() {
        @Override
        public java.sql.Date convert(String source) {
          if (StringUtils.hasLength(source) && !"null".equals(source)) {
            return new java.sql.Date(Long.parseLong(source));
          } else {
            return null;
          }
        }
      });
    }
  }

}
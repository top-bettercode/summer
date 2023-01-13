package top.bettercode.summer.web.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import top.bettercode.summer.logging.annotation.NoRequestLogging;
import top.bettercode.summer.tools.lang.util.TimeUtil;
import top.bettercode.summer.web.DefaultCaptchaServiceImpl;
import top.bettercode.summer.web.ICaptchaService;
import top.bettercode.summer.web.RespEntity;
import top.bettercode.summer.web.error.CustomErrorController;
import top.bettercode.summer.web.error.DataErrorHandler;
import top.bettercode.summer.web.error.DefaultErrorHandler;
import top.bettercode.summer.web.error.ErrorAttributes;
import top.bettercode.summer.web.error.IErrorHandler;
import top.bettercode.summer.web.error.IErrorRespEntityHandler;
import top.bettercode.summer.web.filter.ApiVersionFilter;
import top.bettercode.summer.web.filter.OrderedHiddenHttpMethodFilter;
import top.bettercode.summer.web.filter.OrderedHttpPutFormContentFilter;
import top.bettercode.summer.web.form.FormkeyService;
import top.bettercode.summer.web.form.IFormkeyService;
import top.bettercode.summer.web.kaptcha.KaptchaProperties;
import top.bettercode.summer.web.resolver.ApiExceptionHandlerExceptionResolver;
import top.bettercode.summer.web.resolver.ApiRequestMappingHandlerAdapter;
import top.bettercode.summer.web.serializer.MixIn;
import top.bettercode.summer.web.support.packagescan.PackageScanClassResolver;

/**
 * Rest MVC 配置
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@EnableConfigurationProperties({SummerWebProperties.class, JacksonExtProperties.class})
@AutoConfigureBefore({ErrorMvcAutoConfiguration.class, JacksonAutoConfiguration.class})
public class WebMvcConfiguration {

  private final Logger log = LoggerFactory.getLogger(
      top.bettercode.summer.web.config.WebMvcConfiguration.class);

  private final SummerWebProperties summerWebProperties;
  private final JacksonExtProperties jacksonExtProperties;

  public WebMvcConfiguration(
      SummerWebProperties summerWebProperties,
      JacksonExtProperties jacksonExtProperties) {
    this.summerWebProperties = summerWebProperties;
    this.jacksonExtProperties = jacksonExtProperties;
  }


  /*
   * 响应增加api version
   */
  @Bean
  public ApiVersionFilter apiVersionFilter() {
    return new ApiVersionFilter(summerWebProperties);
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

  @ConditionalOnMissingBean(IFormkeyService.class)
  @Bean
  public IFormkeyService formkeyService() {
    return new FormkeyService(summerWebProperties.getFormExpireSeconds());
  }

  @Bean
  public com.fasterxml.jackson.databind.Module module(GenericApplicationContext applicationContext,
      PackageScanClassResolver packageScanClassResolver) throws ClassNotFoundException {
    SimpleModule module = new SimpleModule();
    Set<String> packages = new HashSet<>(
        Arrays.asList(jacksonExtProperties.getMixInAnnotationBasePackages()));
    String[] beanNames = applicationContext.getBeanNamesForAnnotation(SpringBootApplication.class);
    for (String beanName : beanNames) {
      AbstractBeanDefinition beanDefinition = (AbstractBeanDefinition) applicationContext.getBeanDefinition(
          beanName);
      if (!beanDefinition.hasBeanClass()) {
        beanDefinition.resolveBeanClass(
            top.bettercode.summer.web.config.WebMvcConfiguration.class.getClassLoader());
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

    Set<Class<?>> allSubClasses = packageScanClassResolver
        .findImplementations(MixIn.class, packages.toArray(new String[0]));
    HashMap<Class<?>, Class<?>> targetTypes = new HashMap<>();
    for (Class<?> clazz : allSubClasses) {
      ParameterizedType object = (ParameterizedType) clazz.getGenericInterfaces()[0];
      Class<?> targetType = (Class<?>) object.getActualTypeArguments()[0];
      if (targetTypes.containsKey(targetType)) {
        throw new Error(targetType + " 已存在对应Json MixIn: " + targetTypes.get(targetType));
      }
      targetTypes.put(targetType, clazz);
      if (log.isTraceEnabled()) {
        log.trace("Detected MixInAnnotation:{}=>{}", targetType, clazz);
      }
      module.setMixInAnnotation(targetType, clazz);
    }
    return module;
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnWebApplication
  protected static class ObjectMapperBuilderCustomizer implements
      Jackson2ObjectMapperBuilderCustomizer {

    @Override
    public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
      jacksonObjectMapperBuilder.featuresToEnable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
      jacksonObjectMapperBuilder.serializerByType(LocalDate.class, new JsonSerializer<LocalDate>() {
        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
          gen.writeNumber(TimeUtil.of(value).toMillis());
        }
      });
      jacksonObjectMapperBuilder.deserializerByType(LocalDate.class,
          new JsonDeserializer<LocalDate>() {
            @Override
            public LocalDate deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException {
              String asString = p.getValueAsString();
              if (StringUtils.hasText(asString)) {
                return TimeUtil.of(Long.parseLong(asString)).toLocalDate();
              } else {
                return null;
              }
            }
          });
      jacksonObjectMapperBuilder
          .serializerByType(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            @Override
            public void serialize(LocalDateTime value, JsonGenerator gen,
                SerializerProvider serializers)
                throws IOException {
              gen.writeNumber(TimeUtil.of(value).toMillis());
            }
          });
      jacksonObjectMapperBuilder.deserializerByType(LocalDateTime.class,
          new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException {
              String asString = p.getValueAsString();
              if (StringUtils.hasText(asString)) {
                return TimeUtil.of(Long.parseLong(asString)).toLocalDateTime();
              } else {
                return null;
              }
            }
          });
    }
  }


  @Bean
  public DefaultErrorHandler defaultErrorHandler(MessageSource messageSource,
      @Autowired(required = false) HttpServletRequest request) {
    return new DefaultErrorHandler(messageSource, request);
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(org.springframework.jdbc.UncategorizedSQLException.class)
  @ConditionalOnWebApplication
  protected static class ErrorHandlerConfiguration {

    @Bean
    public DataErrorHandler dataErrorHandler(MessageSource messageSource,
        @Autowired(required = false) HttpServletRequest request) {
      return new DataErrorHandler(messageSource, request);
    }

  }


  @ConditionalOnMissingBean(ErrorAttributes.class)
  @Bean
  public ErrorAttributes errorAttributes(
      @Autowired(required = false) List<IErrorHandler> errorHandlers,
      @Autowired(required = false) IErrorRespEntityHandler errorRespEntityHandler,
      MessageSource messageSource,
      ServerProperties serverProperties) {
    return new ErrorAttributes(serverProperties.getError(), errorHandlers, errorRespEntityHandler,
        messageSource, summerWebProperties);
  }

  @ConditionalOnMissingBean(ErrorController.class)
  @Bean
  public CustomErrorController customErrorController(ErrorAttributes errorAttributes,
      ServerProperties serverProperties,
      @Autowired(required = false) @Qualifier("corsConfigurationSource") CorsConfigurationSource corsConfigurationSource) {
    return new CustomErrorController(errorAttributes, serverProperties.getError(),
        corsConfigurationSource);
  }


  @Bean(name = "error")
  @ConditionalOnMissingBean(name = "error")
  public View error(ObjectMapper objectMapper) {
    return new View() {

      @Override
      public String getContentType() {
        return "text/html;charset=utf-8";
      }

      @Override
      public void render(Map<String, ?> model, @NotNull HttpServletRequest request,
          @NotNull HttpServletResponse response)
          throws Exception {
        if (response.getContentType() == null) {
          response.setContentType(getContentType());
        }
        Boolean isPlainText = (Boolean) request.getAttribute(ErrorAttributes.IS_PLAIN_TEXT_ERROR);
        if (isPlainText != null && isPlainText) {
          response.setContentType(MediaType.TEXT_HTML_VALUE);
          response.getWriter().append((String) model.get(RespEntity.KEY_MESSAGE));
        } else {
          String result = objectMapper.writeValueAsString(model);
          response.setContentType(MediaType.APPLICATION_JSON_VALUE);
          response.getWriter().append(result);
        }
      }
    };
  }


  @Bean
  public WebMvcRegistrations webMvcRegistrations(ErrorAttributes errorAttributes) {
    return new WebMvcRegistrations() {
      @Override
      public RequestMappingHandlerAdapter getRequestMappingHandlerAdapter() {
        return new ApiRequestMappingHandlerAdapter(summerWebProperties, errorAttributes);
      }


      @Override
      public ExceptionHandlerExceptionResolver getExceptionHandlerExceptionResolver() {
        return new ApiExceptionHandlerExceptionResolver(summerWebProperties, errorAttributes);
      }
    };
  }


  @Configuration(proxyBeanMethods = false)
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

      private final Logger log = LoggerFactory.getLogger(CaptchaController.class);
      private final Producer producer;
      private final ICaptchaService captchaService;

      public CaptchaController(Producer producer,
          ICaptchaService captchaService) {
        this.producer = producer;
        this.captchaService = captchaService;
      }

      @NoRequestLogging
      @RequestMapping(value = "/captcha.jpg", name = "图片验证码")
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

}

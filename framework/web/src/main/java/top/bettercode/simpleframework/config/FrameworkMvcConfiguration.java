package top.bettercode.simpleframework.config;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.HandlerMethodReturnValueHandlerComposite;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import top.bettercode.lang.util.TimeUtil;
import top.bettercode.logging.annotation.NoRequestLogging;
import top.bettercode.simpleframework.support.packagescan.PackageScanClassResolver;
import top.bettercode.simpleframework.web.DefaultCaptchaServiceImpl;
import top.bettercode.simpleframework.web.ICaptchaService;
import top.bettercode.simpleframework.web.RespEntity;
import top.bettercode.simpleframework.web.deprecated.DeprecatedAPIInterceptor;
import top.bettercode.simpleframework.web.error.CustomErrorController;
import top.bettercode.simpleframework.web.error.DataErrorHandler;
import top.bettercode.simpleframework.web.error.DefaultErrorHandler;
import top.bettercode.simpleframework.web.error.ErrorAttributes;
import top.bettercode.simpleframework.web.error.IErrorHandler;
import top.bettercode.simpleframework.web.error.IErrorRespEntityHandler;
import top.bettercode.simpleframework.web.filter.ApiVersionFilter;
import top.bettercode.simpleframework.web.filter.OrderedHiddenHttpMethodFilter;
import top.bettercode.simpleframework.web.filter.OrderedHttpPutFormContentFilter;
import top.bettercode.simpleframework.web.form.FormDuplicateCheckInterceptor;
import top.bettercode.simpleframework.web.form.FormkeyService;
import top.bettercode.simpleframework.web.form.IFormkeyService;
import top.bettercode.simpleframework.web.kaptcha.KaptchaProperties;
import top.bettercode.simpleframework.web.resolver.ApiHandlerMethodReturnValueHandler;
import top.bettercode.simpleframework.web.resolver.StringToEnumConverterFactory;
import top.bettercode.simpleframework.web.serializer.MixIn;

/**
 * Rest MVC 配置
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@EnableConfigurationProperties({SummerWebProperties.class, JacksonExtProperties.class})
@AutoConfigureBefore({ErrorMvcAutoConfiguration.class, JacksonAutoConfiguration.class})
public class FrameworkMvcConfiguration {

  private final Logger log = LoggerFactory.getLogger(SerializerConfiguration.class);

  private final SummerWebProperties summerWebProperties;
  private final JacksonExtProperties jacksonExtProperties;

  public FrameworkMvcConfiguration(
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
  public com.fasterxml.jackson.databind.Module module(ApplicationContext applicationContext,
      PackageScanClassResolver packageScanClassResolver) {
    SimpleModule module = new SimpleModule();
    Set<String> packages = PackageScanClassResolver
        .detectPackagesToScan(applicationContext,
            jacksonExtProperties.getMixInAnnotationBasePackages());

    packages.add("top.bettercode.simpleframework.data.serializer");

    Set<Class<?>> allSubClasses = packageScanClassResolver
        .findImplementations(MixIn.class, packages.toArray(new String[0]));
    HashMap<Class<?>, Class<?>> targetTypes = new HashMap<>();
    for (Class<?> aClass : allSubClasses) {
      ParameterizedType object = (ParameterizedType) aClass.getGenericInterfaces()[0];
      Class<?> targetType = (Class<?>) object.getActualTypeArguments()[0];
      if (targetTypes.containsKey(targetType)) {
        throw new Error(targetType + " 已存在对应Json MixIn: " + targetTypes.get(targetType));
      }
      targetTypes.put(targetType, aClass);
      if (log.isTraceEnabled()) {
        log.trace("Detected MixInAnnotation:{}=>{}", targetType, aClass);
      }
      module.setMixInAnnotation(targetType, aClass);
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
      public void render(Map<String, ?> model, HttpServletRequest request,
          HttpServletResponse response)
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
        return new RequestMappingHandlerAdapter() {
          @Override
          public void afterPropertiesSet() {
            super.afterPropertiesSet();

            // Retrieve actual handlers to use as delegate
            HandlerMethodReturnValueHandlerComposite oldHandlers = new HandlerMethodReturnValueHandlerComposite()
                .addHandlers(this.getReturnValueHandlers());

            // Set up ResourceProcessingHandlerMethodResolver to delegate to originally configured ones
            List<HandlerMethodReturnValueHandler> newHandlers = new ArrayList<>();
            newHandlers
                .add(new ApiHandlerMethodReturnValueHandler(oldHandlers, summerWebProperties,
                    errorAttributes));

            // Configure the new handler to be used
            this.setReturnValueHandlers(newHandlers);
          }
        };
      }


      @Override
      public ExceptionHandlerExceptionResolver getExceptionHandlerExceptionResolver() {
        return new ExceptionHandlerExceptionResolver() {
          @Override
          public void afterPropertiesSet() {
            super.afterPropertiesSet();

            // Retrieve actual handlers to use as delegate
            HandlerMethodReturnValueHandlerComposite oldHandlers = this.getReturnValueHandlers();

            // Set up ResourceProcessingHandlerMethodResolver to delegate to originally configured ones
            List<HandlerMethodReturnValueHandler> newHandlers = new ArrayList<>();
            newHandlers
                .add(new ApiHandlerMethodReturnValueHandler(oldHandlers, summerWebProperties,
                    errorAttributes));

            // Configure the new handler to be used
            this.setReturnValueHandlers(newHandlers);
          }
        };
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


  @SuppressWarnings("Convert2Lambda")
  @Configuration(proxyBeanMethods = false)
  @ConditionalOnWebApplication
  protected static class WebMvcConfiguration implements WebMvcConfigurer {

    private final IFormkeyService formkeyService;
    private final MessageSource messageSource;

    private final SummerWebProperties summerWebProperties;

    public WebMvcConfiguration(IFormkeyService formkeyService,
        MessageSource messageSource, SummerWebProperties summerWebProperties) {
      this.formkeyService = formkeyService;
      this.messageSource = messageSource;
      this.summerWebProperties = summerWebProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
      registry.addInterceptor(
              new FormDuplicateCheckInterceptor(formkeyService, summerWebProperties.getFormKeyName()))
          .order(Ordered.LOWEST_PRECEDENCE);
      registry.addInterceptor(new DeprecatedAPIInterceptor(messageSource));
    }

    /**
     * @param registry 注册转换类
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
      registry.addConverterFactory(new StringToEnumConverterFactory());
      registry.addConverter(new Converter<String, Date>() {
        @Override
        public Date convert(String source) {
          if (legalDate(source)) {
            return new Date(Long.parseLong(source));
          } else {
            return null;
          }
        }
      });
      registry.addConverter(new Converter<String, java.sql.Date>() {
        @Override
        public java.sql.Date convert(String source) {
          if (legalDate(source)) {
            return new java.sql.Date(Long.parseLong(source));
          } else {
            return null;
          }
        }
      });
      registry.addConverter(new Converter<String, LocalDate>() {
        @Override
        public LocalDate convert(String source) {
          if (legalDate(source)) {
            return TimeUtil.of(Long.parseLong(source)).toLocalDate();
          } else {
            return null;
          }
        }
      });
      registry.addConverter(new Converter<String, LocalDateTime>() {
        @Override
        public LocalDateTime convert(String source) {
          if (legalDate(source)) {
            return TimeUtil.of(Long.parseLong(source)).toLocalDateTime();
          } else {
            return null;
          }
        }
      });
    }

    private boolean legalDate(String source) {
      return StringUtils.hasLength(source) && !"null".equals(source) && !"0".equals(source);
    }
  }

}

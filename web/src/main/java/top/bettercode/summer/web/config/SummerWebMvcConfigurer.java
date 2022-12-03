package top.bettercode.summer.web.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import javax.validation.constraints.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.bettercode.summer.tools.lang.util.TimeUtil;
import top.bettercode.summer.web.deprecated.DeprecatedAPIInterceptor;
import top.bettercode.summer.web.form.FormDuplicateCheckInterceptor;
import top.bettercode.summer.web.form.IFormkeyService;
import top.bettercode.summer.web.resolver.StringToEnumConverterFactory;

@SuppressWarnings("Convert2Lambda")
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
public class SummerWebMvcConfigurer implements WebMvcConfigurer {

  private final IFormkeyService formkeyService;
  private final MessageSource messageSource;

  private final SummerWebProperties summerWebProperties;

  public SummerWebMvcConfigurer(IFormkeyService formkeyService,
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
      public Date convert(@NotNull String source) {
        if (legalDate(source)) {
          return new Date(Long.parseLong(source));
        } else {
          return null;
        }
      }
    });
    registry.addConverter(new Converter<String, java.sql.Date>() {
      @Override
      public java.sql.Date convert(@NotNull String source) {
        if (legalDate(source)) {
          return new java.sql.Date(Long.parseLong(source));
        } else {
          return null;
        }
      }
    });
    registry.addConverter(new Converter<String, LocalDate>() {
      @Override
      public LocalDate convert(@NotNull String source) {
        if (legalDate(source)) {
          return TimeUtil.of(Long.parseLong(source)).toLocalDate();
        } else {
          return null;
        }
      }
    });
    registry.addConverter(new Converter<String, LocalDateTime>() {
      @Override
      public LocalDateTime convert(@NotNull String source) {
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

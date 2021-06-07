package top.bettercode.util.excel;

import top.bettercode.simpleframework.web.error.IErrorHandler;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnClass(IErrorHandler.class)
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
public class ExcelConfiguration {


  @Bean
  public ExcelErrorHandler excelErrorHandler(MessageSource messageSource,
      @Autowired(required = false) HttpServletRequest request) {
    return new ExcelErrorHandler(messageSource, request);
  }


}
package top.bettercode.summer.tools.excel;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.bettercode.summer.web.error.IErrorHandler;

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
package cn.bestwu.util.excel;

import cn.bestwu.simpleframework.web.error.IErrorHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnClass(IErrorHandler.class)
@Configuration
@ConditionalOnWebApplication
public class ExcelConfiguration {


  @Bean
  public ExcelErrorHandler excelErrorHandler() {
    return new ExcelErrorHandler();
  }


}
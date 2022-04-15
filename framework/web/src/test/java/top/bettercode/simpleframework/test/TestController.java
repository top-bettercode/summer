package top.bettercode.simpleframework.test;


import java.util.Date;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.bettercode.lang.property.Settings;
import top.bettercode.lang.util.StringUtil;
import top.bettercode.simpleframework.support.code.CodeService;
import top.bettercode.simpleframework.support.code.ICodeService;
import top.bettercode.simpleframework.web.BaseController;
import top.bettercode.simpleframework.web.DataDicBean;
import top.bettercode.simpleframework.web.resolver.Cent;
import top.bettercode.simpleframework.web.resolver.CentConverter;
import top.bettercode.simpleframework.web.validator.ChinaCell;

/**
 * @author Peter Wu
 */
@SpringBootApplication
@RestController
@Validated

public class TestController extends BaseController {

  @RequestMapping(value = "/test")
  public Object test( @Validated DataDicBean form,@Cent Long cent, Date a,@ChinaCell String cell) {
    System.err.println(a);
    System.err.println(cent);
    System.err.println(form.getPrice());
    System.err.println(StringUtil.valueOf(form, true));
    DataDicBean dataDicBean = new DataDicBean();
    dataDicBean.setCode("code");
    dataDicBean.setIntCode(1);
    dataDicBean.setPath("/abc.jpg");
    return ok(dataDicBean);
  }

  @RequestMapping(value = "/errors")
  public Object error() {
    throw new RuntimeException("xx");
  }

  @Configuration(proxyBeanMethods = false)
  protected static class CodeConfiguration {

    @Bean
    public CentConverter yuanToCentConverter() {
      return new CentConverter();
    }

    @Bean
    public ICodeService codeNumberService() {
      return new CodeService(Settings.getDicCode());
    }


  }
}
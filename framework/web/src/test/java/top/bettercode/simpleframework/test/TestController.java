package top.bettercode.simpleframework.test;


import top.bettercode.lang.util.StringUtil;
import top.bettercode.simpleframework.support.code.ICodeService;
import top.bettercode.simpleframework.web.BaseController;
import top.bettercode.simpleframework.web.DataDicBean;
import top.bettercode.simpleframework.web.resolver.Cent;
import top.bettercode.simpleframework.web.resolver.CentConverter;
import java.io.Serializable;
import java.util.Date;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Peter Wu
 */
@SpringBootApplication
@RestController
public class TestController extends BaseController {

  @RequestMapping(value = "/test")
  public Object test(DataDicBean form,@Cent Long cent, Date a) {
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
      return new ICodeService() {
        @Override
        public String getName(String codeType, Serializable code) {
          return "name";
        }

        @Override
        public Number getCode(String codeType, String name) {
          return 1;
        }
      };
    }


  }
}
package top.bettercode.summer.web;


import java.util.Date;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.bettercode.summer.tools.lang.property.Settings;
import top.bettercode.summer.tools.lang.util.StringUtil;
import top.bettercode.summer.web.exception.BusinessException;
import top.bettercode.summer.web.form.FormDuplicateCheck;
import top.bettercode.summer.web.resolver.Cent;
import top.bettercode.summer.web.resolver.CentConverter;
import top.bettercode.summer.web.support.code.CodeService;
import top.bettercode.summer.web.support.code.ICodeService;
import top.bettercode.summer.web.validator.ChinaCell;

/**
 * @author Peter Wu
 */
@RestController
@Validated
public class WebTestController extends BaseController {

  @FormDuplicateCheck
  @RequestMapping(value = "/webtest")
  public Object test(@Validated DataDicBean form, @Cent Long cent, Date a, @ChinaCell String cell) {
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

  @FormDuplicateCheck
  @RequestMapping(value = "/weberrors")
  public Object error() {
    throw new BusinessException(String.valueOf(HttpStatus.BAD_GATEWAY.value()), "xx");
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
package top.bettercode.summer.web.support.code;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.util.StringUtils;
import top.bettercode.summer.tools.lang.property.Settings;
import top.bettercode.summer.web.support.ApplicationContextHolder;

/**
 * @author Peter Wu
 */
public class CodeServiceHolder {

  private final static ConcurrentMap<String, ICodeService> CODE_SERVICE_MAP = new ConcurrentHashMap<>();

  public final static ICodeService PROPERTIES_CODESERVICE = new CodeService(Settings.getDicCode());

  public static final String DEFAULT_BEAN_NAME = "defaultCodeService";


  public static ICodeService getDefault() {
    return get(DEFAULT_BEAN_NAME);
  }

  public static ICodeService get(String beanName) {
    ICodeService codeService = CODE_SERVICE_MAP.computeIfAbsent(
        StringUtils.hasText(beanName) ? beanName : DEFAULT_BEAN_NAME,
        s -> ApplicationContextHolder.getBean(s, ICodeService.class));
    return codeService == null ? PROPERTIES_CODESERVICE : codeService;
  }
}

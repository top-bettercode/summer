package top.bettercode.summer.web.form;

import org.springframework.web.method.HandlerMethod;
import top.bettercode.summer.tools.lang.util.AnnotatedUtils;
import top.bettercode.summer.web.servlet.NotErrorHandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 表单重复检
 *
 * @author Peter Wu
 */
public class FormDuplicateCheckInterceptor implements NotErrorHandlerInterceptor {

    public static final String FORM_KEY = FormDuplicateCheckInterceptor.class.getName() + ".form_key";
    public final static String DEFAULT_MESSAGE = "您提交的太快了，请稍候再试。";
    private final IFormkeyService formkeyService;
    private final String formKeyName;

    public FormDuplicateCheckInterceptor(IFormkeyService formkeyService, String formKeyName) {
        this.formkeyService = formkeyService;
        this.formKeyName = formKeyName;
    }


    @Override
    public boolean preHandlerMethod(HttpServletRequest request, HttpServletResponse response,
                                    HandlerMethod handler) {
        FormDuplicateCheck annotation = AnnotatedUtils.getAnnotation(handler, FormDuplicateCheck.class);
        return formkeyService.checkRequest(request, formKeyName, annotation != null, annotation == null ? -1 : annotation.expireSeconds(), annotation == null ? DEFAULT_MESSAGE : annotation.message());
    }


    @Override
    public void afterCompletionMethod(HttpServletRequest request, HttpServletResponse response, HandlerMethod handler, Throwable ex) {
        if (ex == null) {
            ex = getError(request);
        }

        if (ex != null) {
            formkeyService.cleanKey(request);
        }
    }


}

package cn.bestwu.simpleframework.data.resolver;

import cn.bestwu.lang.util.StringUtil;
import cn.bestwu.simpleframework.data.Repositories;
import cn.bestwu.simpleframework.data.RepositoryMetadata;
import cn.bestwu.simpleframework.exception.ResourceNotFoundException;
import cn.bestwu.simpleframework.web.resolver.ModifyModel;
import java.beans.Introspector;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Map;
import javax.servlet.ServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.DataBinder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

/**
 * 资源 Model 参数处理
 * <p>
 *
 * @author Peter Wu
 */
public class ModifyModelMethodArgumentResolver implements HandlerMethodArgumentResolver {

  private final Logger log = LoggerFactory.getLogger(ModifyModelMethodArgumentResolver.class);
  private final Repositories repositories;

  public ModifyModelMethodArgumentResolver(
      Repositories repositories) {
    this.repositories = repositories;
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(ModifyModel.class);
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
    return resolveModel(parameter, mavContainer, webRequest, binderFactory);
  }

  /**
   * 获取 Model
   *
   * @param parameter     parameter
   * @param mavContainer  mavContainer
   * @param webRequest    webRequest
   * @param binderFactory binderFactory
   * @return Object
   * @throws Exception Exception
   */
  protected Object resolveModel(MethodParameter parameter, ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest, WebDataBinderFactory binderFactory)
      throws Exception {
    Object content = getObjectForUpdate(parameter, webRequest, binderFactory);
    if (log.isDebugEnabled()) {
      log.debug("请求原实体：" + StringUtil.valueOf(content));
    }
    Object o = readObject(parameter, content, mavContainer, webRequest, binderFactory);

    if (log.isDebugEnabled()) {
      log.debug("请求实体修改后：" + StringUtil.valueOf(o));
    }
    return o;
  }

  /**
   * @param binder          binder
   * @param idParameterName id参数名
   * @param webRequest      webRequest
   * @param binderFactory   binderFactory
   * @return 实体ID
   */
  private String getId(WebDataBinder binder, String idParameterName, NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {
    Object arg = resolveName(idParameterName, webRequest);

    if (arg == null) {
      arg = webRequest.getParameterValues(idParameterName);
    }

    if (binderFactory != null && arg != null) {
      return binder.convertIfNecessary(arg, String.class);
    }

    return null;
  }

  protected Object resolveName(String name,
      NativeWebRequest request) {
    Map<?, ?> uriTemplateVars = (Map<?, ?>) request
        .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
            RequestAttributes.SCOPE_REQUEST);
    return (uriTemplateVars != null) ? uriTemplateVars.get(name) : null;
  }

  /**
   * @param parameter     parameter
   * @param webRequest    webRequest
   * @param binderFactory binderFactory
   * @return Object 待更新实体
   * @throws Exception Exception
   */
  private Object getObjectForUpdate(MethodParameter parameter, NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) throws Exception {
    ModifyModel modifyModel = parameter.getParameterAnnotation(ModifyModel.class);
    String idParameterName = modifyModel.idParameter();
    WebDataBinder binder = binderFactory.createBinder(webRequest, null,
        idParameterName);
    Serializable id = getId(binder, idParameterName, webRequest, binderFactory);
    if (id == null) {
      throw new IllegalArgumentException("实体主键不能为空");
    }

    Class<?> parameterType = parameter.getParameterType();
    Class<?> modelType = modifyModel.value();
    if (Object.class.equals(modelType)) {
      modelType = parameterType;
    }

    RepositoryMetadata repositoryMetadata = repositories.getRepositoryMetadataFor(modelType);
    Object modelForUpdate = repositoryMetadata
        .invokeFindOne(binder.convertIfNecessary(id, repositoryMetadata.getIdType()));

    if (modelForUpdate == null) {
      throw new ResourceNotFoundException();
    }

    Object oldModel = modelType.getDeclaredConstructor().newInstance();
    BeanUtils.copyProperties(modelForUpdate, oldModel);
    webRequest.setAttribute(ModifyModel.OLD_MODEL, oldModel, NativeWebRequest.SCOPE_REQUEST);

    if (parameterType.equals(modelType)) {
      return modelForUpdate;
    } else {
      Object paramForUpdate = parameterType.getDeclaredConstructor().newInstance();
      BeanUtils.copyProperties(modelForUpdate, paramForUpdate);
      return paramForUpdate;
    }
  }

  /**
   * 从request 读取参数更新到实体
   *
   * @param parameter     parameter
   * @param modelObject   modelObject
   * @param mavContainer  mavContainer
   * @param webRequest    webRequest
   * @param binderFactory binderFactory
   * @return Object
   * @throws Exception Exception
   */
  private Object readObject(MethodParameter parameter, Object modelObject,
      ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) throws Exception {
    Class<?> modelType = modelObject.getClass();
    String name = Introspector.decapitalize(modelType.getSimpleName());

    WebDataBinder binder = binderFactory.createBinder(webRequest, modelObject, name);
    if (binder.getTarget() != null) {
      bindRequestParameters(binder, webRequest);
      validateIfApplicable(binder, parameter);
      if (binder.getBindingResult().hasErrors()) {
        throw new BindException(binder.getBindingResult());
      }
    }

    // Add resolved attribute and BindingResult at the end of the model
    Map<String, Object> bindingResultModel = binder.getBindingResult().getModel();
    mavContainer.removeAttributes(bindingResultModel);
    mavContainer.addAllAttributes(bindingResultModel);

    return binder.convertIfNecessary(binder.getTarget(), modelType);
  }

  /**
   * 如果有必要验证参数
   *
   * @param binder      binder
   * @param methodParam methodParam
   */
  protected void validateIfApplicable(WebDataBinder binder, MethodParameter methodParam) {
    Annotation[] annotations = methodParam.getParameterAnnotations();
    for (Annotation ann : annotations) {
      Validated validatedAnn = AnnotationUtils.getAnnotation(ann, Validated.class);
      if (validatedAnn != null || ann.annotationType().getSimpleName().startsWith("Valid")) {
        Object hints = (validatedAnn != null ? validatedAnn.value()
            : AnnotationUtils.getValue(ann));
        Object[] validationHints = (hints instanceof Object[] ? (Object[]) hints
            : new Object[]{hints});
        binder.validate(validationHints);
        break;
      }
    }
  }

  /**
   * 绑定请求参数
   *
   * @param binder  binder
   * @param request request
   */
  protected void bindRequestParameters(WebDataBinder binder, NativeWebRequest request) {
    ServletRequest servletRequest = request.getNativeRequest(ServletRequest.class);
    ServletRequestDataBinder servletBinder = (ServletRequestDataBinder) binder;
    servletBinder.bind(servletRequest);
  }

  /**
   * 从spring 源码 修改来的
   *
   * @param attributeName attributeName
   * @param modelClass    modelClass
   * @param binderFactory binderFactory
   * @param request       request
   * @return Object
   * @throws Exception Exception
   */
  protected final Object createAttribute(String attributeName, Class<?> modelClass,
      WebDataBinderFactory binderFactory, NativeWebRequest request) throws Exception {

    String value = getRequestValueForAttribute(attributeName, request);
    if (value != null) {
      Object attribute = createAttributeFromRequestValue(
          value, attributeName, modelClass, binderFactory, request);
      if (attribute != null) {
        return attribute;
      }
    }

    return BeanUtils.instantiateClass(modelClass);
  }

  /**
   * 从spring 源码 修改来的
   *
   * @param attributeName attributeName
   * @param request       request
   * @return return
   */
  protected String getRequestValueForAttribute(String attributeName, NativeWebRequest request) {
    if (StringUtils.hasText(request.getParameter(attributeName))) {
      return request.getParameter(attributeName);
    } else {
      return null;
    }
  }

  /**
   * 从spring 源码 修改来的
   *
   * @param sourceValue   sourceValue
   * @param attributeName attributeName
   * @param modelClass    modelClass
   * @param binderFactory binderFactory
   * @param request       request
   * @return Object
   * @throws Exception Exception
   */
  protected Object createAttributeFromRequestValue(String sourceValue, String attributeName,
      Class<?> modelClass, WebDataBinderFactory binderFactory, NativeWebRequest request)
      throws Exception {

    DataBinder binder = binderFactory.createBinder(request, null, attributeName);
    ConversionService conversionService = binder.getConversionService();
    if (conversionService != null) {
      TypeDescriptor source = TypeDescriptor.valueOf(String.class);
      TypeDescriptor target = TypeDescriptor.valueOf(modelClass);
      if (conversionService.canConvert(source, target)) {
        return binder.convertIfNecessary(sourceValue, modelClass);
      }
    }
    return null;
  }
}

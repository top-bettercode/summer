package top.bettercode.summer.web;

import java.lang.annotation.Annotation;
import java.util.Set;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;

/**
 * @author Peter Wu
 */
public class AnnotatedUtils {


  public static <A extends Annotation> boolean hasAnnotation(HandlerMethod handlerMethod,
      Class<A> annotationType) {
    if (handlerMethod.hasMethodAnnotation(annotationType)) {
      return true;
    } else {
      return AnnotatedElementUtils.hasAnnotation(handlerMethod.getBeanType(), annotationType);
    }
  }

  public static <A extends Annotation> A getAnnotation(HandlerMethod handlerMethod,
      Class<A> annotationType) {
    A annotation = handlerMethod.getMethodAnnotation(annotationType);
    return annotation == null ? AnnotatedElementUtils.getMergedAnnotation(
        handlerMethod.getBeanType(), annotationType) : annotation;
  }

  public static <A extends Annotation> Set<A> getAnnotations(HandlerMethod handlerMethod,
      Class<A> annotationType) {
    Set<A> annotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(
        handlerMethod.getMethod(), annotationType);
    return annotations.isEmpty() ? AnnotatedElementUtils.getMergedRepeatableAnnotations(
        handlerMethod.getBeanType(), annotationType) : annotations;
  }

}

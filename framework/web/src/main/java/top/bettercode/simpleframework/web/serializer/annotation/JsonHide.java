package top.bettercode.simpleframework.web.serializer.annotation;

import top.bettercode.simpleframework.web.serializer.HideSerializer;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Peter Wu
 * @since 0.1.15
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = HideSerializer.class)
public @interface JsonHide {

  int beginKeep() default 0;

  int endKeep() default 0;

  boolean alwaysHide() default true;
}

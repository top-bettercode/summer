package top.bettercode.simpleframework.web.serializer.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import top.bettercode.simpleframework.web.serializer.JsonUrlMapper;
import top.bettercode.simpleframework.web.serializer.UrlSerializer;

/**
 * @author Peter Wu
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@Inherited
@JsonSerialize(using = UrlSerializer.class)
public @interface JsonUrl {

  /**
   * @return URL路径前缀配置 expression: e.g. "${summer.multipart.file-url-format}".
   */
  String value() default "";

  /**
   * @return 默认值
   */
  String defaultValue() default "";

  /**
   * @return url字段名称，默认为空表示在原字段后加Url后缀
   */
  String urlFieldName() default "";

  /**
   * @return 是否使用扩展的字段序列化URL
   */
  boolean extended() default true;

  /**
   * @return collection serialize as map
   */
  boolean asMap() default false;

  /**
   * @return 字符串分隔符, 当分隔符不为空时，字符串以分隔符分隔后序列化为数组
   */
  String separator() default "";

  /**
   * @return 对象转换为字符串
   */
  Class<? extends JsonUrlMapper> mapper() default JsonUrlMapper.class;
}

package cn.bestwu.simpleframework.web.serializer.annotation;

import cn.bestwu.simpleframework.web.serializer.YuanDeserializer;
import cn.bestwu.simpleframework.web.serializer.YuanSerializer;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Peter Wu
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = YuanSerializer.class)
@JsonDeserialize(using = YuanDeserializer.class)
public @interface JsonYuan {

}

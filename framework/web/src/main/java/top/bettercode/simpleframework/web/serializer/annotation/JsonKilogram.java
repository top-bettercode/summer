package top.bettercode.simpleframework.web.serializer.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import top.bettercode.simpleframework.web.serializer.KilogramDeserializer;
import top.bettercode.simpleframework.web.serializer.KilogramSerializer;

/**
 * @author Peter Wu
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = KilogramSerializer.class)
@JsonDeserialize(using = KilogramDeserializer.class)
public @interface JsonKilogram {

}

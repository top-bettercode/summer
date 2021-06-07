package top.bettercode.simpleframework.web.serializer.annotation;

import top.bettercode.simpleframework.web.serializer.EmbeddedIdDeserializer;
import top.bettercode.simpleframework.web.serializer.EmbeddedIdSerializer;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
@JsonSerialize(using = EmbeddedIdSerializer.class)
@JsonDeserialize(using = EmbeddedIdDeserializer.class)
public @interface JsonEmbeddedId {

}

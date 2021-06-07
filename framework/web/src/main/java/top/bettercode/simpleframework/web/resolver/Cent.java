package top.bettercode.simpleframework.web.resolver;

import top.bettercode.simpleframework.web.serializer.CentDeserializer;
import top.bettercode.simpleframework.web.serializer.CentSerializer;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 长整形 分 序列化为字符元格式 及反序列化
 *
 * @author Peter Wu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@JacksonAnnotationsInside
@JsonSerialize(using = CentSerializer.class)
@JsonDeserialize(using = CentDeserializer.class)
public @interface Cent {

}

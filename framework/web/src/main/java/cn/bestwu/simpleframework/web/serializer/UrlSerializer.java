package cn.bestwu.simpleframework.web.serializer;

import cn.bestwu.simpleframework.web.serializer.annotation.JsonUrl;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * JSON序列化url自动补全
 *
 * @author Peter Wu
 */
@JacksonStdImpl
public class UrlSerializer extends StdScalarSerializer<Object> implements
    ContextualSerializer {

  private static final long serialVersionUID = 1L;

  private static String defaultFormatExpression = "${app.multipart.file-url-format}";
  private static final JsonUrlMapper defaultMapper = new JsonUrlMapper() {
  };
  private static Environment environment;
  private static final Map<String, String> formatCache = new HashMap<>();
  private static final Map<Class<? extends JsonUrlMapper>, JsonUrlMapper> mapperCache = new HashMap<>();
  private static String defaultFormat;

  private final String formatExpression;
  private String urlFieldName;
  private final boolean useExtensionField;
  private final String separator;
  private final Class<? extends JsonUrlMapper> mapperType;

  public UrlSerializer() {
    this(null, "", true, ",", null);
  }

  public UrlSerializer(String formatExpression, String urlFieldName,
      boolean useExtensionField,
      String separator,
      Class<? extends JsonUrlMapper> mapperType) {
    super(Object.class, false);
    this.formatExpression = formatExpression;
    this.urlFieldName = urlFieldName;
    this.useExtensionField = useExtensionField;
    this.separator = separator;
    this.mapperType = mapperType;
  }

  public static void setDefaultFormatExpression(String defaultFormatExpression) {
    UrlSerializer.defaultFormatExpression = defaultFormatExpression;
  }

  public static void setEnvironment(Environment environment) {
    UrlSerializer.environment = environment;
    defaultFormat = environment.resolvePlaceholders(defaultFormatExpression);
  }

  public static String convert(String path, String formatExpression) {
    if (StringUtils.hasText(path)) {
      String format;
      if (StringUtils.hasText(formatExpression)) {
        format = formatCache.get(formatExpression);
        if (format == null) {
          format = environment.resolvePlaceholders(formatExpression);
          formatCache.put(formatExpression, format);
        }
      } else {
        format = defaultFormat;
      }
      if (!format.contains("%s")) {
        format = format + "%s";
      }
      String url = String.format(format, path);
      if (!url.startsWith("http://") && !url.startsWith("https://")) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
            .getRequestAttributes();
        if (requestAttributes != null) {
          HttpServletRequest request = requestAttributes.getRequest();
          String scheme = request.getScheme();
          String host = request.getHeader(HttpHeaders.HOST);
          if (StringUtils.hasText(host)) {
            url = String.format("%s://%s%s", scheme, host, url);
          }
        }
      }
      return url;
    } else {
      return path;
    }
  }

  public static String convert(String path) {
    return convert(path, null);
  }


  private JsonUrlMapper getMapper() {
    if (mapperType == null || mapperType.equals(JsonUrlMapper.class)) {
      return defaultMapper;
    } else {
      JsonUrlMapper jsonUrlMapper = mapperCache.get(mapperType);
      if (jsonUrlMapper == null) {
        try {
          jsonUrlMapper = mapperType.newInstance();
          mapperCache.put(mapperType, jsonUrlMapper);
        } catch (InstantiationException | IllegalAccessException e) {
          throw new RuntimeException("mapper实例化失败", e);
        }
      }
      return jsonUrlMapper;
    }
  }

  @Override
  public void serialize(Object value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    Class<?> type = value.getClass();
    JsonUrlMapper mapper = getMapper();
    if (type == String.class) {
      if (separator.isEmpty()) {
        String path = mapper.mapper(value);
        if (useExtensionField) {
          JsonStreamContext outputContext = gen.getOutputContext();
          String fieldName = outputContext.getCurrentName();
          gen.writeString(path);
          if ("".equals(urlFieldName)) {
            urlFieldName = fieldName + "Url";
          }
          gen.writeStringField(urlFieldName, convert(path, formatExpression));
        } else {
          gen.writeString(convert(path, formatExpression));
        }
      } else {
        String path = (String) value;
        String[] split = path.split(separator);
        genArray(path, gen, mapper, split);
      }
    } else if (type.isArray()) {
      Object[] array = (Object[]) value;
      genArray(value, gen, mapper, array);
    } else if (Collection.class.isAssignableFrom(type) && !Map.class
        .isAssignableFrom(type)) {
      Collection<?> array = (Collection<?>) value;
      List<String> urls = array.stream().map(mapper::mapper).filter(StringUtils::hasText)
          .map(o -> convert(o, formatExpression)).collect(
              Collectors.toList());
      genCollection(value, gen, urls);
    } else {
      throw new UnsupportedOperationException();
    }

  }

  private void genArray(Object value, JsonGenerator gen, JsonUrlMapper mapper, Object[] array)
      throws IOException {
    List<String> urls = Arrays.stream(array).map(mapper::mapper).filter(StringUtils::hasText)
        .map(s -> convert(s, formatExpression)).collect(
            Collectors.toList());
    genCollection(value, gen, urls);
  }

  private void genCollection(Object value, JsonGenerator gen, List<String> urls)
      throws IOException {
    if (useExtensionField) {
      gen.writeObject(value);
      JsonStreamContext outputContext = gen.getOutputContext();
      String fieldName = outputContext.getCurrentName();
      if ("".equals(urlFieldName)) {
        urlFieldName = fieldName + "Urls";
      }
      gen.writeObjectField(urlFieldName, urls);
    } else {
      gen.writeObject(urls);
    }
  }

  @Override
  public final void serializeWithType(Object value, JsonGenerator gen, SerializerProvider provider,
      TypeSerializer typeSer) throws IOException {
    serialize(value, gen, provider);
  }

  @Override
  public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
    if (property != null) {
      JsonUrl annotation = property.getAnnotation(JsonUrl.class);
      if (annotation == null) {
        throw new RuntimeException("未注解@" + JsonUrl.class.getName());
      }
      return new UrlSerializer(annotation.value(), annotation.urlFieldName(), annotation.extended(),
          annotation.separator(), annotation.mapper());
    }
    return this;
  }
}

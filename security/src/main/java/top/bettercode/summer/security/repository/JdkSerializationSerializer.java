package top.bettercode.summer.security.repository;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.serializer.support.DeserializingConverter;
import org.springframework.core.serializer.support.SerializingConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class JdkSerializationSerializer {

  static final byte[] EMPTY_ARRAY = new byte[0];

  private final Converter<Object, byte[]> serializer;
  private final Converter<byte[], Object> deserializer;

  public JdkSerializationSerializer() {
    this(new SerializingConverter(), new DeserializingConverter());
  }


  public JdkSerializationSerializer(@Nullable ClassLoader classLoader) {
    this(new SerializingConverter(), new DeserializingConverter(classLoader));
  }


  public JdkSerializationSerializer(Converter<Object, byte[]> serializer,
      Converter<byte[], Object> deserializer) {

    Assert.notNull(serializer, "Serializer must not be null!");
    Assert.notNull(deserializer, "Deserializer must not be null!");

    this.serializer = serializer;
    this.deserializer = deserializer;
  }


  public Object deserialize(@Nullable byte[] bytes) {
    if (isEmpty(bytes)) {
      return null;
    }

    try {
      return deserializer.convert(bytes);
    } catch (Exception ex) {
      throw new RuntimeException("Cannot deserialize", ex);
    }
  }

  public byte[] serialize(@Nullable Object object) {
    if (object == null) {
      return EMPTY_ARRAY;
    }
    try {
      return serializer.convert(object);
    } catch (Exception ex) {
      throw new RuntimeException("Cannot serialize", ex);
    }
  }

  public static boolean isEmpty(byte[] bytes) {
    return bytes == null || bytes.length == 0;
  }
}

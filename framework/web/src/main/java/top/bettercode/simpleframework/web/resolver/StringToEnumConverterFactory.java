package top.bettercode.simpleframework.web.resolver;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

/**
 * 数字转Enum
 *
 * @author Peter Wu
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class StringToEnumConverterFactory implements ConverterFactory<String, Enum> {

  @NotNull
  @Override
  public <T extends Enum> Converter<String, T> getConverter(@NotNull Class<T> targetType) {
    Class<?> enumType = targetType;
    while (enumType != null && !enumType.isEnum()) {
      enumType = enumType.getSuperclass();
    }
    if (enumType == null) {
      throw new IllegalArgumentException(
          "The target type " + targetType.getName() + " does not refer to an enum");
    }
    return new StringToEnum(enumType);
  }

  private static class StringToEnum<T extends Enum> implements Converter<String, T> {

    private final Class<T> enumType;

    public StringToEnum(Class<T> enumType) {
      this.enumType = enumType;
    }

    @Override
    public T convert(String source) {
      if (source.length() == 0) {
        // It's an empty enum identifier: reset the enum value to null.
        return null;
      }
      try {
        int i = Integer.parseInt(source);
        return enumType.getEnumConstants()[i];
      } catch (NumberFormatException e) {
        return (T) Enum.valueOf(this.enumType, source.trim());
      }
    }
  }

}
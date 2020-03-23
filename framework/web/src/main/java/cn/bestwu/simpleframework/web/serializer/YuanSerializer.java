package cn.bestwu.simpleframework.web.serializer;

import cn.bestwu.lang.util.MoneyUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.std.NumberSerializer;
import java.io.IOException;
import java.math.BigDecimal;

@JacksonStdImpl
public class YuanSerializer extends NumberSerializer {

  private static final long serialVersionUID = 1759139980737771L;

  private static int newScale = 2;

  public YuanSerializer() {
    super(Long.class);
  }

  public static void setNewScale(int newScale) {
    YuanSerializer.newScale = newScale;
  }

  @Override
  public void serialize(Number value, JsonGenerator gen,
      SerializerProvider provider) throws IOException {
    gen.writeString(MoneyUtil.toYun(new BigDecimal(value.toString()), newScale).toString());
  }


}
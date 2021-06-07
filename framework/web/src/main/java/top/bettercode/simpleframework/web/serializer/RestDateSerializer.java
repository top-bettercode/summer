package top.bettercode.simpleframework.web.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * 剩余多少天到期
 *
 * @author Peter Wu
 */
@JacksonStdImpl
public class RestDateSerializer extends StdScalarSerializer<Date> {

  private static final long serialVersionUID = 1L;

  public RestDateSerializer() {
    super(Date.class, false);
  }

  @Override
  public boolean isEmpty(SerializerProvider provider, Date value) {
    return false;
  }

  @Override
  public JsonNode getSchema(SerializerProvider serializers, Type typeHint) {
    return createSchemaNode("number", true);
  }

  @Override
  public void serialize(Date value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    gen.writeNumber(daysOfTwo(new Date(), value));
  }

  private long daysOfTwo(Date fDate, Date oDate) {
    LocalDateTime localDate = LocalDateTime
        .ofInstant(Instant.ofEpochMilli(fDate.getTime()), ZoneOffset.of("+8"));
    LocalDateTime localDate2 = LocalDateTime
        .ofInstant(Instant.ofEpochMilli(oDate.getTime()), ZoneOffset
            .of("+8"));
    long days = localDate2.toLocalDate().toEpochDay() - localDate.toLocalDate().toEpochDay();
    return days >= 0 ? days : 0;
  }
}

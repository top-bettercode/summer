package com.fasterxml.jackson.datatype.jsr310.ser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import top.bettercode.summer.tools.lang.util.TimeUtil;

/**
 * Serializer for Java 8 temporal {@link LocalDateTime}s.
 */
public class MillisLocalDateTimeSerializer extends JSR310FormattedSerializerBase<LocalDateTime> {

  private static final long serialVersionUID = 1L;

  private final boolean writeDatesAsTimestamps;

  public MillisLocalDateTimeSerializer(boolean writeDatesAsTimestamps) {
    super(LocalDateTime.class);
    this.writeDatesAsTimestamps = writeDatesAsTimestamps;
  }

  private MillisLocalDateTimeSerializer(MillisLocalDateTimeSerializer base, Boolean useTimestamp,
      Boolean useNanoseconds, DateTimeFormatter f,
      JsonFormat.Shape shape, boolean writeDatesAsTimestamps) {
    super(base, useTimestamp, useNanoseconds, f, null);
    this.writeDatesAsTimestamps = writeDatesAsTimestamps;
  }

  @Override
  protected MillisLocalDateTimeSerializer withFormat(Boolean useTimestamp, DateTimeFormatter f,
      JsonFormat.Shape shape) {
    return new MillisLocalDateTimeSerializer(this, useTimestamp, _useNanoseconds, f, shape,
        this.writeDatesAsTimestamps);
  }

  @Override
  protected MillisLocalDateTimeSerializer withFeatures(Boolean writeZoneId,
      Boolean writeNanoseconds) {
    return new MillisLocalDateTimeSerializer(this, _useTimestamp, writeNanoseconds, _formatter,
        null, this.writeDatesAsTimestamps);
  }

  protected DateTimeFormatter _defaultFormatter() {
    return DateTimeFormatter.ISO_LOCAL_DATE_TIME;
  }

  @Override
  public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    try {
      if (this.writeDatesAsTimestamps) {
        gen.writeNumber(TimeUtil.of(value).toMillis());
        return;
      }
    } catch (Exception ignored) {
    }
    if (useTimestamp(provider)) {
      gen.writeStartArray();
      _serializeAsArrayContents(value, gen, provider);
      gen.writeEndArray();
    } else {
      DateTimeFormatter dtf = _formatter;
      if (dtf == null) {
        dtf = _defaultFormatter();
      }
      gen.writeString(value.format(dtf));
    }
  }

  @Override
  public void serializeWithType(LocalDateTime value, JsonGenerator g, SerializerProvider provider,
      TypeSerializer typeSer) throws IOException {
    WritableTypeId typeIdDef = typeSer.writeTypePrefix(g,
        typeSer.typeId(value, serializationShape(provider)));
    // need to write out to avoid double-writing array markers
    if (typeIdDef.valueShape == JsonToken.START_ARRAY) {
      _serializeAsArrayContents(value, g, provider);
    } else {
      DateTimeFormatter dtf = _formatter;
      if (dtf == null) {
        dtf = _defaultFormatter();
      }
      g.writeString(value.format(dtf));
    }
    typeSer.writeTypeSuffix(g, typeIdDef);
  }

  private void _serializeAsArrayContents(LocalDateTime value, JsonGenerator g,
      SerializerProvider provider) throws IOException {
    g.writeNumber(value.getYear());
    g.writeNumber(value.getMonthValue());
    g.writeNumber(value.getDayOfMonth());
    g.writeNumber(value.getHour());
    g.writeNumber(value.getMinute());
    final int secs = value.getSecond();
    final int nanos = value.getNano();
    if ((secs > 0) || (nanos > 0)) {
      g.writeNumber(secs);
      if (nanos > 0) {
        if (useNanoseconds(provider)) {
          g.writeNumber(nanos);
        } else {
          g.writeNumber(value.get(ChronoField.MILLI_OF_SECOND));
        }
      }
    }
  }

  @Override // since 2.9
  protected JsonToken serializationShape(SerializerProvider provider) {
    return useTimestamp(provider) ? JsonToken.START_ARRAY : JsonToken.VALUE_STRING;
  }


}

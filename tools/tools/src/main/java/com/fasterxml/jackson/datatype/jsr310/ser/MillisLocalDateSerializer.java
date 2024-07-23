package com.fasterxml.jackson.datatype.jsr310.ser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import top.bettercode.summer.tools.lang.util.TimeUtil;

/**
 * Serializer for Java 8 temporal {@link LocalDate}s.
 */
public class MillisLocalDateSerializer extends JSR310FormattedSerializerBase<LocalDate> {

  private static final long serialVersionUID = 1L;

  private final boolean writeDatesAsTimestamps;

  public MillisLocalDateSerializer(boolean writeDatesAsTimestamps) {
    super(LocalDate.class);
    this.writeDatesAsTimestamps = writeDatesAsTimestamps;
  }

  protected MillisLocalDateSerializer(MillisLocalDateSerializer base,
      Boolean useTimestamp, DateTimeFormatter dtf, JsonFormat.Shape shape,
      boolean writeDatesAsTimestamps) {
    super(base, useTimestamp, dtf, shape);
    this.writeDatesAsTimestamps = writeDatesAsTimestamps;
  }

  public MillisLocalDateSerializer(DateTimeFormatter formatter, boolean writeDatesAsTimestamps) {
    super(LocalDate.class, formatter);
    this.writeDatesAsTimestamps = writeDatesAsTimestamps;
  }

  @Override
  protected MillisLocalDateSerializer withFormat(Boolean useTimestamp, DateTimeFormatter dtf,
      JsonFormat.Shape shape) {
    return new MillisLocalDateSerializer(this, useTimestamp, dtf, shape, this.writeDatesAsTimestamps);
  }

  @Override
  protected MillisLocalDateSerializer withFeatures(Boolean writeZoneId, Boolean writeNanoseconds) {
    return new MillisLocalDateSerializer(this.writeDatesAsTimestamps);
  }

  @Override
  public void serialize(LocalDate date, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    try {
      if (this.writeDatesAsTimestamps) {
        gen.writeNumber(TimeUtil.of(date).toMillis());
        return;
      }
    } catch (Exception ignored) {
    }
    if (useTimestamp(provider)) {
      if (_shape == JsonFormat.Shape.NUMBER_INT) {
        gen.writeNumber(date.toEpochDay());
      } else {
        gen.writeStartArray();
        _serializeAsArrayContents(date, gen, provider);
        gen.writeEndArray();
      }
    } else {
      gen.writeString((_formatter == null) ? date.toString() : date.format(_formatter));
    }
  }

  @Override
  public void serializeWithType(LocalDate value, JsonGenerator g,
      SerializerProvider provider, TypeSerializer typeSer) throws IOException {
    WritableTypeId typeIdDef = typeSer.writeTypePrefix(g,
        typeSer.typeId(value, serializationShape(provider)));
    // need to write out to avoid double-writing array markers
    switch (typeIdDef.valueShape) {
      case START_ARRAY:
        _serializeAsArrayContents(value, g, provider);
        break;
      case VALUE_NUMBER_INT:
        g.writeNumber(value.toEpochDay());
        break;
      default:
        g.writeString((_formatter == null) ? value.toString() : value.format(_formatter));
    }
    typeSer.writeTypeSuffix(g, typeIdDef);
  }

  protected void _serializeAsArrayContents(LocalDate value, JsonGenerator g,
      SerializerProvider provider) throws IOException {
    g.writeNumber(value.getYear());
    g.writeNumber(value.getMonthValue());
    g.writeNumber(value.getDayOfMonth());
  }

  @Override
  public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint)
      throws JsonMappingException {
    SerializerProvider provider = visitor.getProvider();
    boolean useTimestamp = (provider != null) && useTimestamp(provider);
    if (useTimestamp) {
      _acceptTimestampVisitor(visitor, typeHint);
    } else {
      JsonStringFormatVisitor v2 = visitor.expectStringFormat(typeHint);
      if (v2 != null) {
        v2.format(JsonValueFormat.DATE);
      }
    }
  }

  @Override // since 2.9
  protected JsonToken serializationShape(SerializerProvider provider) {
    if (useTimestamp(provider)) {
      if (_shape == JsonFormat.Shape.NUMBER_INT) {
        return JsonToken.VALUE_NUMBER_INT;
      }
      return JsonToken.START_ARRAY;
    }
    return JsonToken.VALUE_STRING;
  }
}

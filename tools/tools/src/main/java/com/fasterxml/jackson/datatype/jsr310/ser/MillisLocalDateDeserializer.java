package com.fasterxml.jackson.datatype.jsr310.ser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.datatype.jsr310.deser.JSR310DateTimeDeserializerBase;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.util.StringUtils;
import top.bettercode.summer.tools.lang.util.TimeUtil;

/**
 * Deserializer for Java 8 temporal {@link LocalDate}s.
 */
public class MillisLocalDateDeserializer extends JSR310DateTimeDeserializerBase<LocalDate> {

  private static final long serialVersionUID = 1L;

  private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

  private final boolean writeDatesAsTimestamps;

  public MillisLocalDateDeserializer(boolean writeDatesAsTimestamps) {
    this(DEFAULT_FORMATTER, writeDatesAsTimestamps);
  }

  public MillisLocalDateDeserializer(DateTimeFormatter dtf, boolean writeDatesAsTimestamps) {
    super(LocalDate.class, dtf);
    this.writeDatesAsTimestamps = writeDatesAsTimestamps;
  }

  /**
   * Since 2.10
   */
  public MillisLocalDateDeserializer(MillisLocalDateDeserializer base, DateTimeFormatter dtf,
      boolean writeDatesAsTimestamps) {
    super(base, dtf);
    this.writeDatesAsTimestamps = writeDatesAsTimestamps;
  }

  /**
   * Since 2.10
   */
  protected MillisLocalDateDeserializer(MillisLocalDateDeserializer base, Boolean leniency,
      boolean writeDatesAsTimestamps) {
    super(base, leniency);
    this.writeDatesAsTimestamps = writeDatesAsTimestamps;
  }

  /**
   * Since 2.11
   */
  protected MillisLocalDateDeserializer(MillisLocalDateDeserializer base, JsonFormat.Shape shape,
      boolean writeDatesAsTimestamps) {
    super(base, shape);
    this.writeDatesAsTimestamps = writeDatesAsTimestamps;
  }

  @Override
  protected MillisLocalDateDeserializer withDateFormat(DateTimeFormatter dtf) {
    return new MillisLocalDateDeserializer(this, dtf, this.writeDatesAsTimestamps);
  }

  @Override
  protected MillisLocalDateDeserializer withLeniency(Boolean leniency) {
    return new MillisLocalDateDeserializer(this, leniency, this.writeDatesAsTimestamps);
  }

  @Override
  protected MillisLocalDateDeserializer withShape(JsonFormat.Shape shape) {
    return new MillisLocalDateDeserializer(this, shape, this.writeDatesAsTimestamps);
  }

  @Override
  public LocalDate deserialize(JsonParser parser, DeserializationContext context)
      throws IOException {
    try {
      if (writeDatesAsTimestamps) {
        String valueAsString = parser.getValueAsString();
        if (StringUtils.hasText(valueAsString)) {
          return TimeUtil.toLocalDate(parser.getLongValue());
        } else {
          return null;
        }
      }
    } catch (Exception ignored) {
    }

    if (parser.hasToken(JsonToken.VALUE_STRING)) {
      return _fromString(parser, context, parser.getText());
    }
    // 30-Sep-2020, tatu: New! "Scalar from Object" (mostly for XML)
    if (parser.isExpectedStartObjectToken()) {
      return _fromString(parser, context,
          context.extractScalarFromObject(parser, this, handledType()));
    }
    if (parser.isExpectedStartArrayToken()) {
      JsonToken t = parser.nextToken();
      if (t == JsonToken.END_ARRAY) {
        return null;
      }
      if (context.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
          && (t == JsonToken.VALUE_STRING || t == JsonToken.VALUE_EMBEDDED_OBJECT)) {
        final LocalDate parsed = deserialize(parser, context);
        if (parser.nextToken() != JsonToken.END_ARRAY) {
          handleMissingEndArrayForSingle(parser, context);
        }
        return parsed;
      }
      if (t == JsonToken.VALUE_NUMBER_INT) {
        int year = parser.getIntValue();
        int month = parser.nextIntValue(-1);
        int day = parser.nextIntValue(-1);

        if (parser.nextToken() != JsonToken.END_ARRAY) {
          throw context.wrongTokenException(parser, handledType(), JsonToken.END_ARRAY,
              "Expected array to end");
        }
        return LocalDate.of(year, month, day);
      }
      context.reportInputMismatch(handledType(),
          "Unexpected token (%s) within Array, expected VALUE_NUMBER_INT",
          t);
    }
    if (parser.hasToken(JsonToken.VALUE_EMBEDDED_OBJECT)) {
      return (LocalDate) parser.getEmbeddedObject();
    }
    // 06-Jan-2018, tatu: Is this actually safe? Do users expect such coercion?
    if (parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
      // issue 58 - also check for NUMBER_INT, which needs to be specified when serializing.
      if (_shape == JsonFormat.Shape.NUMBER_INT || isLenient()) {
        return LocalDate.ofEpochDay(parser.getLongValue());
      }
      return _failForNotLenient(parser, context, JsonToken.VALUE_STRING);
    }
    return _handleUnexpectedToken(context, parser, "Expected array or string.");
  }

  protected LocalDate _fromString(JsonParser p, DeserializationContext ctxt,
      String string0) throws IOException {
    String string = string0.trim();
    if (string.length() == 0) {
      // 22-Oct-2020, tatu: not sure if we should pass original (to distinguish
      //   b/w empty and blank); for now don't which will allow blanks to be
      //   handled like "regular" empty (same as pre-2.12)
      return _fromEmptyString(p, ctxt, string);
    }
    try {
      // as per [datatype-jsr310#37], only check for optional (and, incorrect...) time marker 'T'
      // if we are using default formatter
      DateTimeFormatter format = _formatter;
      if (format == DEFAULT_FORMATTER) {
        // JavaScript by default includes time in JSON serialized Dates (UTC/ISO instant format).
        if (string.length() > 10 && string.charAt(10) == 'T') {
          if (isLenient()) {
            if (string.endsWith("Z")) {
              return LocalDate.parse(string.substring(0, string.length() - 1),
                  DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
            return LocalDate.parse(string, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
          }
          JavaType t = getValueType(ctxt);
          return (LocalDate) ctxt.handleWeirdStringValue(t.getRawClass(),
              string,
              "Should not contain time component when 'strict' mode set for property or type (enable 'lenient' handling to allow)"
          );
        }
      }
      return LocalDate.parse(string, format);
    } catch (DateTimeException e) {
      return _handleDateTimeException(ctxt, e, string);
    }
  }
}

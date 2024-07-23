package com.fasterxml.jackson.datatype.jsr310.ser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.LocalDate;
import top.bettercode.summer.tools.lang.serializer.PlusDays;

/**
 * Deserializer for Java 8 temporal {@link LocalDate}s.
 */
public class PlusDaysMillisLocalDateDeserializer extends StdDeserializer<LocalDate> implements
    ContextualDeserializer {

  private final MillisLocalDateDeserializer delegate;
  private final Long daysToAdd;

  public PlusDaysMillisLocalDateDeserializer(MillisLocalDateDeserializer delegate) {
    this(delegate, 0L);
  }

  protected PlusDaysMillisLocalDateDeserializer(MillisLocalDateDeserializer delegate,
      Long daysToAdd) {
    super(LocalDate.class);
    this.delegate = delegate;
    this.daysToAdd = daysToAdd;
  }


  @Override
  public LocalDate deserialize(JsonParser parser, DeserializationContext context)
      throws IOException {
    LocalDate localDate = delegate.deserialize(parser, context);
    if (daysToAdd != 0) {
      return localDate.plusDays(daysToAdd);
    } else {
      return localDate;
    }
  }

  @Override
  public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
      throws JsonMappingException {
    JsonDeserializer<?> contextual = delegate.createContextual(ctxt, property);
    if (property == null) {
      return contextual;
    }
    PlusDays annotation = property.getAnnotation(PlusDays.class);
    if (annotation == null) {
      return contextual;
    } else {
      return new PlusDaysMillisLocalDateDeserializer((MillisLocalDateDeserializer) contextual,
          annotation.value());
    }
  }
}

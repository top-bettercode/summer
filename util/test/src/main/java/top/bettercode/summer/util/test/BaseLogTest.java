package top.bettercode.summer.util.test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.AfterAll;
import org.slf4j.impl.StaticLoggerBinder;

public abstract class BaseLogTest {

  @AfterAll
  static void logAfterAll() {
    ((LoggerContext) StaticLoggerBinder.getSingleton().getLoggerFactory()).getLogger(
        "org.hibernate.SQL").setLevel(
        Level.OFF);
  }

}


package top.bettercode.summer.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * logging smtp 配置
 *
 * @author Peter Wu
 */
@ConfigurationProperties("summer.logging.smtp")
public class SmtpProperties {

  private String[] logger = {"root"};
  private String from;
  private String marker;
  private String to;
  private String username;
  private String password;
  private String filter = "ERROR";
  private String localhost;
  private String host;
  private int port = 25;
  private boolean starttls = false;
  private boolean ssl = false;
  private boolean sessionViaJNDI = false;
  private String jndiLocation = "java:comp/env/mail/Session";
  private boolean includeCallerData = false;
  private boolean asynchronousSending = true;
  private String charsetEncoding = "UTF-8";

  public String[] getLogger() {
    return logger;
  }

  public void setLogger(String[] logger) {
    this.logger = logger;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getMarker() {
    return marker;
  }

  public void setMarker(String marker) {
    this.marker = marker;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getFilter() {
    return filter;
  }

  public void setFilter(String filter) {
    this.filter = filter;
  }

  public String getLocalhost() {
    return localhost;
  }

  public void setLocalhost(String localhost) {
    this.localhost = localhost;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public boolean isStarttls() {
    return starttls;
  }

  public void setStarttls(boolean starttls) {
    this.starttls = starttls;
  }

  public boolean isSsl() {
    return ssl;
  }

  public void setSsl(boolean ssl) {
    this.ssl = ssl;
  }

  public boolean isSessionViaJNDI() {
    return sessionViaJNDI;
  }

  public void setSessionViaJNDI(boolean sessionViaJNDI) {
    this.sessionViaJNDI = sessionViaJNDI;
  }

  public String getJndiLocation() {
    return jndiLocation;
  }

  public void setJndiLocation(String jndiLocation) {
    this.jndiLocation = jndiLocation;
  }

  public boolean isIncludeCallerData() {
    return includeCallerData;
  }

  public void setIncludeCallerData(boolean includeCallerData) {
    this.includeCallerData = includeCallerData;
  }

  public boolean isAsynchronousSending() {
    return asynchronousSending;
  }

  public void setAsynchronousSending(boolean asynchronousSending) {
    this.asynchronousSending = asynchronousSending;
  }

  public String getCharsetEncoding() {
    return charsetEncoding;
  }

  public void setCharsetEncoding(String charsetEncoding) {
    this.charsetEncoding = charsetEncoding;
  }
}
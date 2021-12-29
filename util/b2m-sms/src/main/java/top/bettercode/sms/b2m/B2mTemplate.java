package top.bettercode.sms.b2m;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableMap;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RequestCallback;
import top.bettercode.lang.util.AESUtil;
import top.bettercode.lang.util.StringUtil;
import top.bettercode.simpleframework.support.client.ApiTemplate;

/**
 * 亿美软通短信平台 接口请求
 */
public class B2mTemplate extends ApiTemplate {

  public static final String LOG_MARKER = "b2s";
  private final Logger log = LoggerFactory.getLogger(B2mTemplate.class);
  private final B2mProperties b2mProperties;

  public B2mTemplate(
      B2mProperties b2mProperties) {
    super("第三方接口", "亿美软通短信平台", LOG_MARKER, b2mProperties.getConnectTimeout(),
        b2mProperties.getReadTimeout());
    this.b2mProperties = b2mProperties;
    MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter() {
      @Override
      protected boolean canRead(MediaType mediaType) {
        return true;
      }

      @Override
      public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return true;
      }
    };
    ObjectMapper objectMapper = messageConverter.getObjectMapper();
//    objectMapper.setConfig(objectMapper.getSerializationConfig().with(JsonWriteFeature.ESCAPE_NON_ASCII));

    objectMapper.setDefaultPropertyInclusion(Include.NON_NULL);
    List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
    messageConverters.add(new ByteArrayHttpMessageConverter());
    messageConverters.add(new AllEncompassingFormHttpMessageConverter());
    messageConverters.add(messageConverter);
    setMessageConverters(messageConverters);
  }


  /**
   * 个性短信接口
   *
   * <p>文档：http://www.b2m.cn/static/doc/sms/personalizedsms_or.html</p>
   * <p>示例：http://ip:port/simpleinter/sendPersonalitySMS?appId=EUCP-EMY-DDDD-3EEEE&timestamp=20170101120000&sign=PIEUDJI987EUID62PKEDSESQEDSEDFSE&extendedCode=123&timerTime=20171211022000&customSmsId=10001&18001098901=天气不错1啊&18001098902=天气不错2啊
   * </p>
   *
   * @param content 手机号=内容(必填)【可多个】 以手机号为参数名，内容为参数值传输 如：18001000000=端午节快乐,(最多500个)
   * @return 结果
   */
  public List<B2mRespData> sendPersonalitySMS(Map<String, String> content) {
    String timestamp = getTimestamp();
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("appId", b2mProperties.getAppId());
    params.add("timestamp", timestamp);
    //    格式：md5(appId+ secretKey + timestamp) 32位
    params.add("sign", DigestUtils.md5DigestAsHex(
        (b2mProperties.getAppId() + b2mProperties.getSecretKey() + timestamp).getBytes(
            StandardCharsets.UTF_8)));

    content.forEach((key, value) -> {
      if (StringUtils.hasText(key) && StringUtils.hasText(value)) {
        params.add(key, value);
      }
    });
    params.add("timerTime", "");
    params.add("customSmsId", "");
    params.add("extendedCode", "");

    RequestCallback requestCallback = httpEntityCallback(new HttpEntity<>(params),
        B2mResponse.class);

    ResponseEntity<B2mResponse> entity;
    try {
      entity = execute(b2mProperties.getUrl() + "/simpleinter/sendPersonalitySMS", HttpMethod.POST,
          requestCallback,
          responseEntityExtractor(B2mResponse.class));
    } catch (Exception e) {
      throw new B2mException(e);
    }
    if (entity == null) {
      throw new B2mException();
    }
    if (entity.getStatusCode().is2xxSuccessful()) {
      B2mResponse body = entity.getBody();
      if (body.isOk()) {
        return body.getData();
      } else {
        String message = body.getMessage();
        throw new B2mSysException(message == null ? "请求失败" : message);
      }
    } else {
      throw new B2mException();
    }
  }


  /**
   * 安全接口 个性短信接口【全属性个性】
   *
   * <p>文档：http://www.b2m.cn/static/doc/sms/personalizedsmsall.html</p>
   *
   * @param content 手机号=内容(必填)【可多个】 以手机号为参数名，内容为参数值传输 如：18001000000=端午节快乐,(最多500个)
   * @return 结果
   */
  public List<B2mRespData> sendPersonalityAllSMS(Map<String, String> content) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("appId", b2mProperties.getAppId());
    headers.add("gzip", "on");

    Map<String, Object> params = new HashMap<>();
    List<Map<String, Object>> smses = new ArrayList<>();
    content.forEach((key, value) -> smses.add(ImmutableMap.of(
//          "customSmsId", "",
//          "timerTime", "",
//          "extendedCode", "",
        "mobile", key,
        "content", value
    )));
    params.put("smses", smses);
    params.put("requestTime", System.currentTimeMillis());
    params.put("requestValidPeriod", b2mProperties.getRequestValidPeriod());

    String json = StringUtil.json(params);
    log.info(MarkerFactory.getMarker(LOG_MARKER), "params:{}", json);
    byte[] data = json.getBytes(StandardCharsets.UTF_8);
    data = StringUtil.gzip(data);
    data = AESUtil.encrypt(data, b2mProperties.getSecretKey());

    RequestCallback requestCallback = httpEntityCallback(new HttpEntity<>(data, headers),
        byte[].class);

    ResponseEntity<byte[]> entity;
    try {
      entity = execute(b2mProperties.getUrl() + "/inter/sendPersonalityAllSMS", HttpMethod.POST,
          requestCallback,
          responseEntityExtractor(byte[].class));
    } catch (Exception e) {
      throw new B2mException(e);
    }
    if (entity == null) {
      throw new B2mException();
    }
    if (entity.getStatusCode().is2xxSuccessful()) {
      String code = entity.getHeaders().getFirst("result");
      if (B2mResponse.SUCCESS.equals(code)) {
        byte[] respData = entity.getBody();
        respData = AESUtil.decrypt(respData, b2mProperties.getSecretKey());
        respData = StringUtil.ungzip(respData);
        log.info(MarkerFactory.getMarker(LOG_MARKER), "result:{}", new String(respData));
        return StringUtil.readJson(respData,
            TypeFactory.defaultInstance().constructCollectionType(List.class, B2mRespData.class));
      } else {
        String message = B2mResponse.getMessage(code);
        throw new B2mSysException(message == null ? "请求失败" : message);
      }
    } else {
      throw new B2mException();
    }
  }

  /**
   * @return 格式：yyyyMMddHHmmss 14位
   */
  private String getTimestamp() {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
  }

}

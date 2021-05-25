package cn.bestwu.summer.util.test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import cn.bestwu.autodoc.gen.Autodoc;
import cn.bestwu.autodoc.gen.AutodocSetting;
import cn.bestwu.logging.RequestLoggingFilter;
import cn.bestwu.logging.RequestLoggingProperties;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.WebApplicationContext;

/**
 * mockMvc 基础测试类
 *
 * @author Peter Wu
 */
@ExtendWith(value = {SpringExtension.class, AutodocSetting.class})
@SpringBootTest(properties = {
    "logging.websocket.enabled=false",
    "spring.jackson.default-property-inclusion=ALWAYS",
    "logging.level.root=debug",
    "logging.level.org.springframework.test=warn",
    "logging.level.org.springframework.boot.test=warn",
    "logging.slack.channel=",
    "logging.request.timeout-alarm-seconds=30"})
public abstract class BaseWebNoAuthTest {

  @Autowired
  private WebApplicationContext context;
  protected MockMvc mockMvc;
  @Autowired
  private RequestLoggingFilter requestLoggingFilter;
  @Autowired(required = false)
  private AutoDocFilter autoDocFilter;
  @Autowired
  protected RequestLoggingProperties requestLoggingProperties;

  protected ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  public void setup() throws Exception {

    //--------------------------------------------
    requestLoggingProperties.setForceRecord(true);
    requestLoggingProperties.setIncludeRequestBody(true);
    requestLoggingProperties.setIncludeResponseBody(true);
    requestLoggingProperties.setFormat(true);
    mockMvc = webAppContextSetup(context)
        .addFilter(autoDocFilter)
        .addFilter(requestLoggingFilter)
        .build();
  }

  private String getFileName(MvcResult result) throws UnsupportedEncodingException {
    String contentDisposition = result.getResponse().getHeader("Content-Disposition");
    contentDisposition = URLDecoder
        .decode(contentDisposition.replaceAll(".*filename\\*=UTF-8''(.*?)", "$1"), "UTF-8");
    return "build/" + contentDisposition;
  }

  protected void download(ResultActions perform) throws Exception {
    MvcResult result = perform.andExpect(status().isOk()).andReturn();
    String fileName = getFileName(result);
    StreamUtils.copy(result.getResponse().getContentAsByteArray(),
        new FileOutputStream(fileName));
    try {
      String filePath = System.getProperty("user.dir") + File.separator + fileName;
      if (System.getProperties().getProperty("os.name").toLowerCase().startsWith("win")) {
        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + filePath);
      } else {
        Runtime.getRuntime().exec("xdg-open " + filePath);
      }
    } catch (Exception ignored) {
    }
  }

  protected String nonnullJson(Object object) throws JsonProcessingException {
    return objectMapper.setSerializationInclusion(
        Include.NON_NULL).writeValueAsString(object);
  }

  protected void requires(String... require) {
    Autodoc.requiredParameters(require);
  }

  protected void tableNames(String... tableName) {
    Autodoc.tableNames(tableName);
  }
}

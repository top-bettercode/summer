package top.bettercode.summer.util.test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.WebApplicationContext;
import top.bettercode.autodoc.gen.Autodoc;
import top.bettercode.logging.RequestLoggingFilter;
import top.bettercode.logging.RequestLoggingProperties;

/**
 * mockMvc 基础测试类
 *
 * @author Peter Wu
 */
@ExtendWith(value = {SpringExtension.class})
@SpringBootTest
@TestPropertySource(properties = {
    "summer.security.enabled=false"
})
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
  @Autowired
  protected ObjectMapper objectMapper;

  @BeforeEach
  public void setup() throws Exception {
    //--------------------------------------------
    requestLoggingProperties.setForceRecord(true);
    requestLoggingProperties.setIncludeRequestBody(true);
    requestLoggingProperties.setIncludeResponseBody(true);
    requestLoggingProperties.setFormat(true);
    mockMvc = mockMvcBuilder().build();
  }

  @NotNull
  protected DefaultMockMvcBuilder mockMvcBuilder() {
    return webAppContextSetup(context)
        .addFilter(autoDocFilter)
        .addFilter(requestLoggingFilter);
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

  protected String json(Object object) throws JsonProcessingException {
    return json(object, Include.NON_NULL);
  }

  protected String json(Object object, JsonInclude.Include incl) throws JsonProcessingException {
    return objectMapper.setSerializationInclusion(incl).writeValueAsString(object);
  }

  protected void requires(String... require) {
    Autodoc.requiredParameters(require);
  }

  protected void tableNames(String... tableName) {
    Autodoc.tableNames(tableName);
  }
}

package top.bettercode.summer.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import top.bettercode.summer.logging.RequestLoggingFilter;
import top.bettercode.summer.logging.RequestLoggingProperties;
import top.bettercode.summer.test.autodoc.Autodoc;
import top.bettercode.summer.web.config.SummerWebProperties;
import top.bettercode.summer.web.error.CustomErrorController;
import top.bettercode.summer.web.support.ApplicationContextHolder;

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
public abstract class BaseWebNoAuthTest extends MockMvcRequestBuilders {

  @Autowired
  private WebApplicationContext context;
  protected MockMvc mockMvc;
  @Autowired
  private RequestLoggingFilter requestLoggingFilter;
  @Autowired(required = false)
  private AutoDocFilter autoDocFilter;
  @Autowired
  private SummerWebProperties webProperties;
  @Autowired
  private CustomErrorController errorController;
  @Autowired
  protected RequestLoggingProperties requestLoggingProperties;
  protected final ObjectMapper objectMapper = new ObjectMapper();

  @AfterAll
  static void logAfterAll() {
    ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(
        "org.hibernate.SQL").setLevel(
        Level.OFF);
  }

  @BeforeEach
  public void setup() throws Exception {
    //--------------------------------------------
    requestLoggingProperties.setForceRecord(true);
    requestLoggingProperties.setIncludeRequestBody(true);
    requestLoggingProperties.setIncludeResponseBody(true);
    requestLoggingProperties.setFormat(true);
    mockMvc = mockMvcBuilder().build();
    defaultBeforeEach();
    System.err.println("------------------------------------------------------");
  }

  protected void defaultBeforeEach() throws Exception {

  }

  protected void beforeEach() throws Exception {

  }


  protected boolean embeddedDatabase() {
    return !StringUtils.hasText(ApplicationContextHolder.getProperty("spring.datasource.url"));
  }

  @NotNull
  protected DefaultMockMvcBuilder mockMvcBuilder() {
    return webAppContextSetup(context)
        .addFilter(autoDocFilter)
        .addFilter(requestLoggingFilter)
        .addFilter(new TestErrorPageFilter(errorController, webProperties))
        ;
  }


  @NotNull
  protected ResultMatcher contentStatusIsOk() {
    return result -> assertTrue(contentAsJsonNode(result).get("status").asInt() < 400);
  }

  @NotNull
  protected ResultMatcher contentStatus(int status) {
    return result -> assertEquals(contentAsJsonNode(result).get("status").asInt(), status);
  }

  protected JsonNode contentAsJsonNode(MvcResult result) throws IOException {
    return objectMapper.readTree(result.getResponse().getContentAsByteArray());
  }

  private String getFileName(MvcResult result) throws UnsupportedEncodingException {
    String contentDisposition = result.getResponse().getHeader("Content-Disposition");
    contentDisposition = URLDecoder
        .decode(
            Objects.requireNonNull(contentDisposition)
                .replaceAll(".*filename\\*=UTF-8''(.*?)", "$1"), "UTF-8");
    return "build/" + contentDisposition;
  }

  protected ResultActions perform(RequestBuilder requestBuilder) throws Exception {
    return mockMvc.perform(requestBuilder
    ).andExpect(status().isOk()).andExpect(contentStatusIsOk());
  }

  protected ResultActions performRest(RequestBuilder requestBuilder) throws Exception {
    return mockMvc.perform(requestBuilder
    ).andExpect(status().isOk());
  }

  protected void download(RequestBuilder requestBuilder, String fileName) throws Exception {
    download(mockMvc.perform(requestBuilder), "build/" + fileName);
  }

  protected void download(RequestBuilder requestBuilder) throws Exception {
    download(mockMvc.perform(requestBuilder), null);
  }

  protected void download(ResultActions perform, String fileName) throws Exception {
    MvcResult result = perform.andExpect(status().isOk()).andReturn();
    if (fileName == null) {
      fileName = getFileName(result);
    }
    StreamUtils.copy(result.getResponse().getContentAsByteArray(),
        Files.newOutputStream(Paths.get(fileName)));
    try {
      String filePath = System.getProperty("user.dir") + File.separator + fileName;
      if (System.getProperties().getProperty("os.name").toLowerCase().startsWith("win")) {
        Runtime.getRuntime()
            .exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", filePath});
      } else {
        Runtime.getRuntime().exec(new String[]{"xdg-open", filePath});
      }
    } catch (Exception ignored) {
    }
  }

  protected MockMultipartFile file(String name, String classPath) throws IOException {
    ClassPathResource classPathResource = new ClassPathResource(classPath);
    return new MockMultipartFile(name, classPathResource.getFilename(), null,
        classPathResource.getInputStream());
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

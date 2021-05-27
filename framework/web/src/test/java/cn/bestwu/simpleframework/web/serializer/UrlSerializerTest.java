package cn.bestwu.simpleframework.web.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cn.bestwu.logging.operation.PrettyPrintingContentModifier;
import cn.bestwu.simpleframework.config.JacksonExtProperties;
import cn.bestwu.simpleframework.web.DataDicBean;
import cn.bestwu.simpleframework.web.serializer.annotation.JsonUrl;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

/**
 * @author Peter Wu
 */
public class UrlSerializerTest {

  private JacksonExtProperties jacksonExtProperties = new JacksonExtProperties();
  final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  public void setUp() {
    objectMapper.setDefaultPropertyInclusion(Include.NON_NULL);
    objectMapper.setSerializerFactory(objectMapper.getSerializerFactory()
        .withSerializerModifier(new CustomNullSerializerModifier(jacksonExtProperties)));

    MockEnvironment mockEnvironment = new MockEnvironment();
//    mockEnvironment.setProperty("summer.multipart.file-url-format", "/path%s");
    mockEnvironment.setProperty("summer.multipart.file-url-format", "http://127.0.0.1%s");
    mockEnvironment.setProperty("path1-url", "http://127.0.0.2%s");
    UrlSerializer.setEnvironment(mockEnvironment);
  }

  @Test
  void convert() {
    System.err.println(UrlSerializer.convert("/adb"));
  }

  @Test
  public void serializeExtend() throws Exception {
    objectMapper.addMixIn(DataDicBean.class, DataDicBeanMinExtend.class);
    DataDicBean dicBean = new DataDicBean();
    String path = "/abc.jpg";
    dicBean.setPath(path);
    dicBean.setPath1(path);
    assertEquals(
        "{\"path\":\"/abc.jpg\",\"pathUrl\":\"http://127.0.0.1/abc.jpg\",\"path1\":\"/abc.jpg\",\"path1Url\":\"http://127.0.0.2/abc.jpg\"}",
        objectMapper.writeValueAsString(dicBean));

    dicBean.setPath("");
    dicBean.setPath("  ");
    dicBean.setPath1(null);
    assertEquals("{\"path\":\"\",\"pathUrl\":\"\"}", objectMapper.writeValueAsString(dicBean));
  }

  @Test
  public void serialize() throws Exception {
    objectMapper.addMixIn(DataDicBean.class, DataDicBeanMin.class);
    DataDicBean dicBean = new DataDicBean();
    String path = "/abc.jpg";
    dicBean.setPath(path);
    dicBean.setPath1(path);
    assertEquals(
        "{\"path\":\"http://127.0.0.1/abc.jpg\",\"path1\":\"http://127.0.0.2/abc.jpg\"}",
        objectMapper.writeValueAsString(dicBean));

    dicBean.setPath("");
    dicBean.setPath("  ");
    dicBean.setPath1(null);
    assertEquals("{\"path\":\"\"}", objectMapper.writeValueAsString(dicBean));
  }

  @Test
  public void serializeArrayStringExtend() throws Exception {
    objectMapper.addMixIn(DataDicBean.class, DataDicBeanMinStringExtend.class);
    DataDicBean dicBean = new DataDicBean();
    String path = "/abc.jpg,/124.jpg";
    dicBean.setPath(path);
    dicBean.setPath1(path);
    System.err.println(objectMapper.writeValueAsString(dicBean));
    assertEquals(
        "{\"path\":\"/abc.jpg,/124.jpg\",\"pathUrls\":[\"http://127.0.0.1/abc.jpg\",\"http://127.0.0.1/124.jpg\"],\"path1\":\"/abc.jpg,/124.jpg\",\"path1Urls\":[\"http://127.0.0.2/abc.jpg\",\"http://127.0.0.2/124.jpg\"]}",
        objectMapper.writeValueAsString(dicBean));
  }

  @Test
  public void serializeArrayString() throws Exception {
    objectMapper.addMixIn(DataDicBean.class, DataDicBeanMinString.class);
    DataDicBean dicBean = new DataDicBean();
    String path = "/abc.jpg,/124.jpg";
    dicBean.setPath(path);
    dicBean.setPath1(path);
    assertEquals(
        "{\"path\":[\"http://127.0.0.1/abc.jpg\",\"http://127.0.0.1/124.jpg\"],\"path1\":[\"http://127.0.0.2/abc.jpg\",\"http://127.0.0.2/124.jpg\"]}",
        objectMapper.writeValueAsString(dicBean));
  }

  @Test
  public void serializeArrayStringExtendAsMap() throws Exception {
    objectMapper.addMixIn(DataDicBean.class, DataDicBeanMinStringExtendAsMap.class);
    DataDicBean dicBean = new DataDicBean();
    String path = "/abc.jpg,/124.jpg";
    dicBean.setPath(path);
    dicBean.setPath1(path);
    String actual = objectMapper.writeValueAsString(dicBean);
    System.err.println(actual);
    assertEquals(
        "{\"path\":\"/abc.jpg,/124.jpg\",\"pathUrls\":[{\"path\":\"/abc.jpg\",\"pathUrl\":\"http://127.0.0.1/abc.jpg\"},{\"path\":\"/124.jpg\",\"pathUrl\":\"http://127.0.0.1/124.jpg\"}],\"path1\":\"/abc.jpg,/124.jpg\",\"path1Urls\":[{\"path\":\"/abc.jpg\",\"pathUrl\":\"http://127.0.0.2/abc.jpg\"},{\"path\":\"/124.jpg\",\"pathUrl\":\"http://127.0.0.2/124.jpg\"}]}",
        actual);
  }

  @NotNull
  private String prettyStr(String actual) {
    return new String(PrettyPrintingContentModifier.INSTANCE.modifyContent(actual.getBytes(
        StandardCharsets.UTF_8)));
  }

  @Test
  public void serializeArrayStringAsMap() throws Exception {
    objectMapper.addMixIn(DataDicBean.class, DataDicBeanMinStringAsMap.class);
    DataDicBean dicBean = new DataDicBean();
    String path = "/abc.jpg,/124.jpg";
    dicBean.setPath(path);
    dicBean.setPath1(path);
    String actual = objectMapper.writeValueAsString(dicBean);
    System.err.println((actual));
    assertEquals(
        "{\"path\":[{\"path\":\"/abc.jpg\",\"pathUrl\":\"http://127.0.0.1/abc.jpg\"},{\"path\":\"/124.jpg\",\"pathUrl\":\"http://127.0.0.1/124.jpg\"}],\"path1\":[{\"path\":\"/abc.jpg\",\"pathUrl\":\"http://127.0.0.2/abc.jpg\"},{\"path\":\"/124.jpg\",\"pathUrl\":\"http://127.0.0.2/124.jpg\"}]}",
        actual);
  }

  @Test
  public void serializeArrayExtend() throws Exception {
    objectMapper.addMixIn(DataDicBean.class, DataDicBeanMinExtend.class);

    DataDicBean dicBean = new DataDicBean();
    ArrayList<String> paths = new ArrayList<>();
    paths.add("/abc.jpg");
    paths.add("/def.jpg");
    paths.add(" ");
    dicBean.setPathArray(paths.toArray(new String[0]));
    assertEquals(
        "{\"pathArray\":[\"/abc.jpg\",\"/def.jpg\",\" \"],\"pathArrayUrls\":[\"http://127.0.0.1/abc.jpg\",\"http://127.0.0.1/def.jpg\"],\"pathArray1\":[\"/abc.jpg\",\"/def.jpg\",\" \"],\"pathArray1Urls\":[\"http://127.0.0.2/abc.jpg\",\"http://127.0.0.2/def.jpg\"]}",
        objectMapper.writeValueAsString(dicBean));

    dicBean.setPathArray(null);
    assertEquals("{}", objectMapper.writeValueAsString(dicBean));

  }

  @Test
  public void serializeArray() throws Exception {
    objectMapper.addMixIn(DataDicBean.class, DataDicBeanMin.class);

    DataDicBean dicBean = new DataDicBean();
    ArrayList<String> paths = new ArrayList<>();
    paths.add("/abc.jpg");
    paths.add("/def.jpg");
    paths.add(" ");
    dicBean.setPathArray(paths.toArray(new String[0]));
    assertEquals(
        "{\"pathArray\":[\"http://127.0.0.1/abc.jpg\",\"http://127.0.0.1/def.jpg\"],\"pathArray1\":[\"http://127.0.0.2/abc.jpg\",\"http://127.0.0.2/def.jpg\"]}",
        objectMapper.writeValueAsString(dicBean));

    dicBean.setPathArray(null);
    assertEquals("{}", objectMapper.writeValueAsString(dicBean));

  }

  @Test
  public void serializeCollectionExtend() throws Exception {
    objectMapper.addMixIn(DataDicBean.class, DataDicBeanMinExtend.class);

    DataDicBean dicBean = new DataDicBean();
    dicBean.setPaths(new ArrayList<>());
    dicBean.getPaths().add("/abc.jpg");
    dicBean.getPaths().add("/def.jpg");
    dicBean.getPaths().add(" ");
    assertEquals(
        "{\"paths\":[\"/abc.jpg\",\"/def.jpg\",\" \"],\"pathsUrls\":[\"http://127.0.0.1/abc.jpg\",\"http://127.0.0.1/def.jpg\"],\"paths1\":[\"/abc.jpg\",\"/def.jpg\",\" \"],\"paths1Urls\":[\"http://127.0.0.2/abc.jpg\",\"http://127.0.0.2/def.jpg\"]}",
        objectMapper.writeValueAsString(dicBean));

    dicBean.setPaths(null);
    assertEquals("{}", objectMapper.writeValueAsString(dicBean));

  }

  @Test
  public void serializeCollection() throws Exception {
    objectMapper.addMixIn(DataDicBean.class, DataDicBeanMin.class);

    DataDicBean dicBean = new DataDicBean();
    dicBean.setPaths(new ArrayList<>());
    dicBean.getPaths().add("/abc.jpg");
    dicBean.getPaths().add("/def.jpg");
    dicBean.getPaths().add(" ");
    assertEquals(
        "{\"paths\":[\"http://127.0.0.1/abc.jpg\",\"http://127.0.0.1/def.jpg\"],\"paths1\":[\"http://127.0.0.2/abc.jpg\",\"http://127.0.0.2/def.jpg\"]}",
        objectMapper.writeValueAsString(dicBean));

    dicBean.setPaths(null);
    assertEquals("{}", objectMapper.writeValueAsString(dicBean));

  }

  interface DataDicBeanMinExtend {

    @JsonUrl
    List<String> getPaths();

    @JsonUrl("${path1-url}")
    List<String> getPaths1();

    @JsonUrl
    String[] getPathArray();

    @JsonUrl("${path1-url}")
    String[] getPathArray1();

    @JsonUrl
    String getPath();

    @JsonUrl("${path1-url}")
    String getPath1();
  }

  interface DataDicBeanMinStringExtend {

    @JsonUrl(separator = ",")
    String getPath();

    @JsonUrl(value = "${path1-url}", separator = ",")
    String getPath1();
  }

  interface DataDicBeanMinStringExtendAsMap {

    @JsonUrl(separator = ",", asMap = true)
    String getPath();

    @JsonUrl(value = "${path1-url}", separator = ",", asMap = true)
    String getPath1();
  }

  interface DataDicBeanMinString {

    @JsonUrl(separator = ",", extended = false)
    String getPath();

    @JsonUrl(value = "${path1-url}", separator = ",", extended = false)
    String getPath1();
  }

  interface DataDicBeanMinStringAsMap {

    @JsonUrl(separator = ",", extended = false, asMap = true)
    String getPath();

    @JsonUrl(value = "${path1-url}", separator = ",", extended = false, asMap = true)
    String getPath1();
  }

  interface DataDicBeanMin {

    @JsonUrl(extended = false)
    List<String> getPaths();

    @JsonUrl(value = "${path1-url}", extended = false)
    List<String> getPaths1();

    @JsonUrl(extended = false)
    String[] getPathArray();

    @JsonUrl(value = "${path1-url}", extended = false)
    String[] getPathArray1();

    @JsonUrl(extended = false)
    String getPath();

    @JsonUrl(value = "${path1-url}", extended = false)
    String getPath1();
  }


}
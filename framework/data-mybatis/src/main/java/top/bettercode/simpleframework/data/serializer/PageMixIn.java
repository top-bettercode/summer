package top.bettercode.simpleframework.data.serializer;

import top.bettercode.simpleframework.web.serializer.MixIn;
import com.baomidou.mybatisplus.plugins.Page;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import java.util.List;

/**
 * Page MixIn
 *
 * @param <T> T
 * @author Peter Wu
 */
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({
    "searchCount", "openSort", "condition", "optimizeCountSql", "ascs", "descs", "isAsc",
    "orderByField"
})
public interface PageMixIn<T> extends MixIn<Page> {

  @JsonProperty("page")
  @JsonView(Object.class)
  int getCurrent();

  @JsonView(Object.class)
  int getSize();

  @JsonView(Object.class)
  long getTotal();

  @JsonView(Object.class)
  int getPages();

  @JsonProperty("list")
  @JsonView(Object.class)
  List<T> getRecords();
}
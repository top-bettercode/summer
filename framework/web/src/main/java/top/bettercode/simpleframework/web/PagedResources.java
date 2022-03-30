package top.bettercode.simpleframework.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import java.util.Collection;
import java.util.Objects;
import org.springframework.util.Assert;

public class PagedResources<T> {

  private Collection<T> content;

  private PageMetadata metadata;

  public PagedResources() {
  }

  public PagedResources(PageMetadata metadata, Collection<T> content) {
    this.content = content;
    this.metadata = metadata;
  }

  @JsonView(Object.class)
  public Collection<T> getContent() {
    return content;
  }

  public void setContent(Collection<T> content) {
    this.content = content;
  }

  @JsonView(Object.class)
  public PageMetadata getPage() {
    return metadata;
  }

  public void setPage(PageMetadata metadata) {
    this.metadata = metadata;
  }

  public static class PageMetadata {

    @JsonView(Object.class)
    @JsonProperty
    private long number;
    @JsonView(Object.class)
    @JsonProperty
    private long size;
    @JsonView(Object.class)
    @JsonProperty
    private long totalPages;
    @JsonView(Object.class)
    @JsonProperty
    private long totalElements;

    public PageMetadata() {

    }

    public PageMetadata(long number, long size, long totalPages, long totalElements) {

      Assert.isTrue(number > -1, "Number must not be negative!");
      Assert.isTrue(size > -1, "Size must not be negative!");
      Assert.isTrue(totalElements > -1, "Total elements must not be negative!");
      Assert.isTrue(totalPages > -1, "Total pages must not be negative!");

      this.number = number;
      this.size = size;
      this.totalPages = totalPages;
      this.totalElements = totalElements;
    }

    public PageMetadata(long number, long size, long totalElements) {
      this(number, size, size == 0 ? 0 : (long) Math.ceil((double) totalElements / (double) size),
          totalElements);
    }

    public long getSize() {
      return size;
    }

    public long getTotalElements() {
      return totalElements;
    }

    public long getTotalPages() {
      return totalPages;
    }

    public long getNumber() {
      return number;
    }

    public void setSize(long size) {
      this.size = size;
    }

    public void setTotalElements(long totalElements) {
      this.totalElements = totalElements;
    }

    public void setTotalPages(long totalPages) {
      this.totalPages = totalPages;
    }

    public void setNumber(long number) {
      this.number = number;
    }

    @Override
    public String toString() {
      return String
          .format("Metadata { number: %d, total pages: %d, total elements: %d, size: %d }", number,
              totalPages, totalElements, size);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof PageMetadata)) {
        return false;
      }
      PageMetadata that = (PageMetadata) o;
      return number == that.number && size == that.size && totalPages == that.totalPages
          && totalElements == that.totalElements;
    }

    @Override
    public int hashCode() {
      return Objects.hash(number, size, totalPages, totalElements);
    }
  }
}
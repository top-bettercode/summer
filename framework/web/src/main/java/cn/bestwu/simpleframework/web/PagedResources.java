package cn.bestwu.simpleframework.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import java.util.Collection;
import javax.xml.bind.annotation.XmlAttribute;
import org.springframework.util.Assert;

public class PagedResources<T> {

  private Collection<T> content;

  private PageMetadata metadata;

  public PagedResources() {
  }

  public PagedResources(Collection<T> content,
      PageMetadata metadata) {
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
    @XmlAttribute
    @JsonProperty
    private long size;
    @JsonView(Object.class)
    @XmlAttribute
    @JsonProperty
    private long totalElements;
    @JsonView(Object.class)
    @XmlAttribute
    @JsonProperty
    private long totalPages;
    @JsonView(Object.class)
    @XmlAttribute
    @JsonProperty
    private long number;

    public PageMetadata() {

    }

    public PageMetadata(long size, long number, long totalElements, long totalPages) {

      Assert.isTrue(size > -1, "Size must not be negative!");
      Assert.isTrue(number > -1, "Number must not be negative!");
      Assert.isTrue(totalElements > -1, "Total elements must not be negative!");
      Assert.isTrue(totalPages > -1, "Total pages must not be negative!");

      this.size = size;
      this.number = number;
      this.totalElements = totalElements;
      this.totalPages = totalPages;
    }

    public PageMetadata(long size, long number, long totalElements) {
      this(size, number, totalElements,
          size == 0 ? 0 : (long) Math.ceil((double) totalElements / (double) size));
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
    public boolean equals(Object obj) {

      if (this == obj) {
        return true;
      }

      if (obj == null || !obj.getClass().equals(getClass())) {
        return false;
      }

      PageMetadata that = (PageMetadata) obj;

      return this.number == that.number && this.size == that.size
          && this.totalElements == that.totalElements
          && this.totalPages == that.totalPages;
    }

    @Override
    public int hashCode() {

      int result = 17;
      result += 31 * (int) (this.number ^ this.number >>> 32);
      result += 31 * (int) (this.size ^ this.size >>> 32);
      result += 31 * (int) (this.totalElements ^ this.totalElements >>> 32);
      result += 31 * (int) (this.totalPages ^ this.totalPages >>> 32);
      return result;
    }
  }
}
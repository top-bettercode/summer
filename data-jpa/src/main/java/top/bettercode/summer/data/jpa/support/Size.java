package top.bettercode.summer.data.jpa.support;

import org.springframework.data.domain.Sort;

/**
 * @author Peter Wu
 */
public class Size {
  private final int size;
  private final Sort sort;

  public static Size of(int size) {
    return of(size, Sort.unsorted());
  }

  public static Size of(int size, Sort sort) {
    return new Size(size, sort);
  }

  private Size(int size, Sort sort) {
    this.size = size;
    this.sort = sort;
  }

  public int getSize() {
    return size;
  }

  public Sort getSort() {
    return sort;
  }
}

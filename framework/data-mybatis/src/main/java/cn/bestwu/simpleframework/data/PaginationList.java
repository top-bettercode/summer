package cn.bestwu.simpleframework.data;

import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Wu
 */
public class PaginationList<T> extends ArrayList<T> implements List<T>{
  private static final long serialVersionUID = 1L;
  private Pagination pagination;

  public PaginationList(Pagination pagination, List<T> records) {
    super(records);
    this.pagination = pagination;
  }

  public Pagination getPagination() {
    return pagination;
  }

  public void setPagination(Pagination pagination) {
    this.pagination = pagination;
  }

}

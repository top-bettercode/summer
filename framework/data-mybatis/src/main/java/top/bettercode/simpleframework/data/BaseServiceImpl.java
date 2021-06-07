package top.bettercode.simpleframework.data;

import com.baomidou.mybatisplus.entity.TableInfo;
import com.baomidou.mybatisplus.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.toolkit.TableInfoHelper;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.PostConstruct;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/**
 * @author Peter Wu
 */
public class BaseServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements
    IBaseService<M, T> {

  @Autowired
  private Repositories repositories;
  private Map<String, String> cachedFieldsMap;
  protected Logger log = LoggerFactory.getLogger(getClass());

  @PostConstruct
  public void init() {
    RepositoryMetadata repositoryMetadata = repositories
        .getRepositoryMetadataFor(currentModelClass());
    this.cachedFieldsMap = repositoryMetadata == null ? new HashMap<>() : repositoryMetadata
        .getCachedFieldsMap();
  }

  @Override
  public M getRepository() {
    return baseMapper;
  }

  @Override
  public boolean deleteByPropertyMap(Map<String, Object> propertyMap) {
    return super.deleteByMap(convert2ColumnMap(propertyMap));
  }

  @Override
  @NotNull
  public Map<String, Object> convert2ColumnMap(Map<String, Object> propertyMap) {
    Map<String, Object> columnMap = new HashMap<>();
    for (Entry<String, Object> entry : propertyMap.entrySet()) {
      columnMap.put(cachedFieldsMap.get(entry.getKey()), entry.getValue());
    }
    return columnMap;
  }

  @Override
  public List<T> selectByPropertyMap(Map<String, Object> propertyMap) {
    return super.selectByMap(convert2ColumnMap(propertyMap));
  }

  protected String like(String keyword) {
    return StringUtils.hasText(keyword) ? ("%" + keyword + "%") : null;
  }

  protected String sort(String sort) {
    return "asc".equalsIgnoreCase(sort) ? "asc" : "desc";
  }

  @Override
  public String getColumnName(String propertyName) {
    return cachedFieldsMap.get(propertyName);
  }

  @Override
  public boolean insertOrUpdate(T entity) {
    if (null != entity) {
      Class<?> cls = entity.getClass();
      TableInfo tableInfo = TableInfoHelper.getTableInfo(cls);
      if (tableInfo == null) {
        tableInfo = TableInfoHelper.getTableInfo(cls.getSuperclass());
      }
      return insertOrUpdate(entity, cls, tableInfo);
    }
    return false;
  }

  private boolean insertOrUpdate(T entity, Class<?> cls, TableInfo tableInfo) {
    if (null != tableInfo && com.baomidou.mybatisplus.toolkit.StringUtils
        .isNotEmpty(tableInfo.getKeyProperty())) {
      Object idVal = ReflectionKit.getMethodValue(cls, entity, tableInfo.getKeyProperty());
      if (com.baomidou.mybatisplus.toolkit.StringUtils.checkValNull(idVal)) {
        return insert(entity);
      } else {
        /*
         * 更新成功直接返回，失败执行插入逻辑
         */
        return updateById(entity) || insert(entity);
      }
    } else {
      throw new MybatisPlusException("Error:  Can not execute. Could not find @TableId.");
    }
  }

  @Override
  public boolean insertOrUpdate(T entity, Class<?> cls) {
    if (null != entity) {
      TableInfo tableInfo = TableInfoHelper.getTableInfo(cls);
      return insertOrUpdate(entity, cls, tableInfo);
    }
    return false;
  }


  public static <T> Page<T> page(List<T> list) {
    Page<T> page = new Page<>();
    page.setRecords(list);
    if (list instanceof PaginationList) {
      Pagination pagination = ((PaginationList<?>) list).getPagination();
      page.setCurrent(pagination.getCurrent());
      page.setSize(pagination.getSize());
      page.setTotal(pagination.getTotal());
    } else if (list instanceof Collection) {
      page.setCurrent(1);
      page.setSize(list.size());
      page.setTotal(list.size());
    }

    return page;
  }
}

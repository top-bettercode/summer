package top.bettercode.simpleframework.data;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.service.IService;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * @author Peter Wu
 */
public interface IBaseService<M extends BaseMapper<T>, T> extends IService<T> {

  M getRepository();

  boolean deleteByPropertyMap(Map<String, Object> propertyMap);

  @NotNull
  Map<String, Object> convert2ColumnMap(Map<String, Object> propertyMap);

  List<T> selectByPropertyMap(Map<String, Object> propertyMap);

  String getColumnName(String propertyName);

  boolean insertOrUpdate(T entity, Class<?> cls);
}

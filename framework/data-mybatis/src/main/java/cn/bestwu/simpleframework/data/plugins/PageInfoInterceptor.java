package cn.bestwu.simpleframework.data.plugins;

import cn.bestwu.simpleframework.data.PaginationList;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.plugins.pagination.PageHelper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.baomidou.mybatisplus.toolkit.StringUtils;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

@Intercepts({
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
        RowBounds.class, ResultHandler.class}),
    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class,
        Integer.class})})
public class PageInfoInterceptor extends PaginationInterceptor implements Interceptor {

  /**
   * 是否开启 PageHelper localPage 模式
   */
  private boolean localPage = false;

  /**
   * Physical Pagination Interceptor for all the queries with parameter {@link
   * org.apache.ibatis.session.RowBounds}
   */
  @SuppressWarnings("unchecked")
  @Override
  public Object intercept(Invocation invocation) throws Throwable {

    Object target = invocation.getTarget();
    if (target instanceof StatementHandler) {
      return super.intercept(invocation);
    } else {
      RowBounds rowBounds = (RowBounds) invocation.getArgs()[2];
      if (rowBounds == null || rowBounds == RowBounds.DEFAULT) {
        // 本地线程分页
        if (localPage) {
          // 采用ThreadLocal变量处理的分页
          rowBounds = PageHelper.getPagination();
          if (rowBounds == null) {
            return invocation.proceed();
          }
        } else {
          return invocation.proceed();
        }
      }
      Object proceed = invocation.proceed();
      if (rowBounds instanceof Pagination) {
        List records = (List) proceed;
        if (rowBounds instanceof Page) {
          ((Page) rowBounds).setRecords(records);
        }
        return new PaginationList<>((Pagination) rowBounds, records);
      } else {
        return proceed;
      }
    }
  }

  @Override
  public Object plugin(Object target) {
    if (target instanceof Executor) {
      return Plugin.wrap(target, this);
    }
    if (target instanceof StatementHandler) {
      return Plugin.wrap(target, this);
    }
    return target;
  }

  @Override
  public void setProperties(Properties prop) {
    String dialectType = prop.getProperty("dialectType");
    String dialectClazz = prop.getProperty("dialectClazz");
    String localPage = prop.getProperty("localPage");

    if (StringUtils.isNotEmpty(dialectType)) {
      super.setDialectType(dialectType);
    }
    if (StringUtils.isNotEmpty(dialectClazz)) {
      super.setDialectType(dialectClazz);
    }
    if (StringUtils.isNotEmpty(localPage)) {
      this.localPage = Boolean.valueOf(localPage);
      super.setLocalPage(this.localPage);
    }
  }

  @Override
  public PaginationInterceptor setLocalPage(boolean localPage) {
    this.localPage = localPage;
    super.setLocalPage(localPage);
    return this;
  }
}

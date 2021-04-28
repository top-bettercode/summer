package cn.bestwu.simpleframework.data.jpa.querydsl;

import cn.bestwu.simpleframework.data.jpa.config.JpaExtProperties;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.QuerydslJpaPredicateExecutor;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * @author Peter Wu
 */
public class QuerydslJpaExtPredicateExecutor<T> extends QuerydslJpaPredicateExecutor<T> implements
    QuerydslPredicateExecutor<T>, RecycleQuerydslPredicateExecutor<T> {

  private final QuerydslSoftDeleteSupport softDeleteSupport;

  public QuerydslJpaExtPredicateExecutor(
      JpaExtProperties jpaExtProperties,
      JpaEntityInformation<T, ?> entityInformation,
      EntityManager entityManager,
      EntityPathResolver resolver,
      CrudMethodMetadata metadata) {
    super(entityInformation, entityManager, resolver, metadata);
    this.softDeleteSupport = new QuerydslSoftDeleteSupport(jpaExtProperties,
        entityInformation.getJavaType(), resolver.createPath(entityInformation.getJavaType()));
  }

  @Override
  public Optional<T> findOne(Predicate predicate) {
    if (softDeleteSupport.support()) {
      predicate = softDeleteSupport.andFalsePredicate(predicate);
    }
       return super.findOne(predicate);
  }

  @Override
  public List<T> findAll(Predicate predicate) {
    if (softDeleteSupport.support()) {
      predicate = softDeleteSupport.andFalsePredicate(predicate);
    }
    return super.findAll(predicate);
  }

  @Override
  public List<T> findAll(Predicate predicate, OrderSpecifier<?>... orders) {
    if (softDeleteSupport.support()) {
      predicate = softDeleteSupport.andFalsePredicate(predicate);
    }
    return super.findAll(predicate, orders);
  }

  @Override
  public List<T> findAll(Predicate predicate, Sort sort) {
    if (softDeleteSupport.support()) {
      predicate = softDeleteSupport.andFalsePredicate(predicate);
    }
    return super.findAll(predicate, sort);
  }

  @Override
  public List<T> findAll(OrderSpecifier<?>... orders) {
    if (softDeleteSupport.support()) {
      return super.findAll(softDeleteSupport.andFalsePredicate(null), orders);
    } else {
      return super.findAll(orders);
    }
  }

  @Override
  public Page<T> findAll(Predicate predicate, Pageable pageable) {
    if (softDeleteSupport.support()) {
      predicate = softDeleteSupport.andFalsePredicate(predicate);
    }
    return super.findAll(predicate, pageable);
  }

  @Override
  public long count(Predicate predicate) {
    if (softDeleteSupport.support()) {
      predicate = softDeleteSupport.andFalsePredicate(predicate);
    }
    return super.count(predicate);
  }

  @Override
  public boolean exists(Predicate predicate) {
    if (softDeleteSupport.support()) {
      predicate = softDeleteSupport.andFalsePredicate(predicate);
    }
    return super.exists(predicate);
  }

  @Override
  public Optional<T> findOneFromRecycleBin(Predicate predicate) {
    if (softDeleteSupport.support()) {
      predicate = softDeleteSupport.andTruePredicate(predicate);
      return super.findOne(predicate);
    } else {
      return Optional.empty();
    }
  }

  @Override
  public Iterable<T> findAllFromRecycleBin(Predicate predicate) {
    if (softDeleteSupport.support()) {
      predicate = softDeleteSupport.andTruePredicate(predicate);
      return super.findAll(predicate);
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public Iterable<T> findAllFromRecycleBin(Predicate predicate, Sort sort) {
    if (softDeleteSupport.support()) {
      predicate = softDeleteSupport.andTruePredicate(predicate);
      return super.findAll(predicate, sort);
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public Iterable<T> findAllFromRecycleBin(Predicate predicate, OrderSpecifier<?>... orders) {
    if (softDeleteSupport.support()) {
      predicate = softDeleteSupport.andTruePredicate(predicate);
      return super.findAll(predicate, orders);
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public Iterable<T> findAllFromRecycleBin(OrderSpecifier<?>... orders) {
    if (softDeleteSupport.support()) {
      return super.findAll(softDeleteSupport.andTruePredicate(null), orders);
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public Page<T> findAllFromRecycleBin(Predicate predicate, Pageable pageable) {
    if (softDeleteSupport.support()) {
      predicate = softDeleteSupport.andTruePredicate(predicate);
      return super.findAll(predicate, pageable);
    } else {
      return Page.empty();
    }
  }

  @Override
  public long countRecycleBin(Predicate predicate) {
    if (softDeleteSupport.support()) {
      predicate = softDeleteSupport.andTruePredicate(predicate);
      return super.count(predicate);
    } else {
      return 0;
    }
  }

  @Override
  public boolean existsInRecycleBin(Predicate predicate) {
    if (softDeleteSupport.support()) {
      predicate = softDeleteSupport.andTruePredicate(predicate);
      return super.exists(predicate);
    } else {
      return false;
    }
  }
}

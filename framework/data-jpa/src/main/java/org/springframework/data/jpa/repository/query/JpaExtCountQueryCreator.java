package org.springframework.data.jpa.repository.query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.ReturnedType;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.lang.Nullable;
import top.bettercode.simpleframework.data.jpa.support.ExtJpaSupport;

/**
 * @author Peter Wu
 */
public class JpaExtCountQueryCreator extends JpaCountQueryCreator {

  private final ExtJpaSupport softDeleteSupport;

  public JpaExtCountQueryCreator(PartTree tree,
      ReturnedType type,
      CriteriaBuilder builder,
      ParameterMetadataProvider provider,
      ExtJpaSupport softDeleteSupport) {
    super(tree, type, builder, provider);
    this.softDeleteSupport = softDeleteSupport;
  }

  @Override
  protected CriteriaQuery<?> complete(@Nullable Predicate predicate, Sort sort,
      CriteriaQuery<?> query, CriteriaBuilder builder, Root<?> root) {
    if (predicate != null && softDeleteSupport.supportSoftDeleted()) {
      Path<Boolean> deletedPath = root.get(softDeleteSupport.getSoftDeletedPropertyName());
      predicate = builder.and(predicate, builder.isFalse(deletedPath));
    }
    return super.complete(predicate, sort, query, builder, root);
  }
}

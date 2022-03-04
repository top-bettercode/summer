package org.springframework.data.jpa.repository.query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.ReturnedType;
import org.springframework.data.repository.query.parser.PartTree;
import top.bettercode.simpleframework.data.jpa.support.SoftDeleteSupport;

/**
 * @author Peter Wu
 */
public class JpaExtCountQueryCreator extends JpaCountQueryCreator {
  private final SoftDeleteSupport softDeleteSupport;

  public JpaExtCountQueryCreator(PartTree tree,
      ReturnedType type,
      CriteriaBuilder builder,
      ParameterMetadataProvider provider,
      SoftDeleteSupport softDeleteSupport) {
    super(tree, type, builder, provider);
    this.softDeleteSupport = softDeleteSupport;
  }

  @Override
  protected CriteriaQuery<?> complete(Predicate predicate, Sort sort,
      CriteriaQuery<?> query, CriteriaBuilder builder, Root<?> root) {
    if (softDeleteSupport.support()) {
      Path<Boolean> deletedPath = root.get(softDeleteSupport.getPropertyName());
      predicate = builder.and(predicate, builder.isFalse(deletedPath));
    }
    return super.complete(predicate, sort, query, builder, root);
  }
}

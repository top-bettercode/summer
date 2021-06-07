package org.springframework.data.jpa.repository.query;

import top.bettercode.simpleframework.data.jpa.support.SoftDeleteSupport;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.ReturnedType;
import org.springframework.data.repository.query.parser.PartTree;

/**
 * @author Peter Wu
 */
public class JpaExtQueryCreator extends JpaQueryCreator {

  private final SoftDeleteSupport softDeleteSupport;

  /**
   * Create a new {@link JpaQueryCreator}.
   *
   * @param tree must not be {@literal null}.
   * @param type must not be {@literal null}.
   * @param builder must not be {@literal null}.
   * @param provider must not be {@literal null}.
   * @param softDeleteSupport softDeleteSupport
   */
  public JpaExtQueryCreator(PartTree tree,
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

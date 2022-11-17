package top.bettercode.simpleframework.data.jpa.support;

import static org.springframework.data.jpa.repository.query.QueryUtils.COUNT_QUERY_STRING;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import top.bettercode.lang.util.BeanUtil;
import top.bettercode.simpleframework.data.jpa.JpaExtRepository;
import top.bettercode.simpleframework.data.jpa.config.JpaExtProperties;

/**
 * @author Peter Wu
 */
public class SimpleJpaExtRepository<T, ID> extends
    SimpleJpaRepository<T, ID> implements JpaExtRepository<T, ID> {

  private final Logger sqlLog = LoggerFactory.getLogger("org.hibernate.SQL");
  public static final String SOFT_DELETE_ALL_QUERY_STRING = "update %s e set e.%s = :%s where e.%s = :%s";
  private static final String EQUALS_CONDITION_STRING = "%s.%s = :%s";

  private final JpaEntityInformation<T, ID> entityInformation;
  private final EntityManager em;
  private final PersistenceProvider provider;
  private final ExtJpaSupport extJpaSupport;
  private final EscapeCharacter escapeCharacter = EscapeCharacter.DEFAULT;
  private final Object notDeleted;
  private final Object deleted;
  private final Specification<T> notDeletedSpec;
  private final Specification<T> deletedSpec;

  public SimpleJpaExtRepository(
      JpaExtProperties jpaExtProperties,
      JpaEntityInformation<T, ID> entityInformation, EntityManager entityManager) {
    super(entityInformation, entityManager);
    this.entityInformation = entityInformation;
    this.em = entityManager;
    this.provider = PersistenceProvider.fromEntityManager(entityManager);
    this.extJpaSupport = new DefaultExtJpaSupport(jpaExtProperties, getDomainClass());
    this.notDeleted = extJpaSupport.getSoftDeletedFalseValue();
    this.deleted = extJpaSupport.getSoftDeletedTrueValue();
    this.notDeletedSpec = (root, query, builder) -> builder.equal(
        root.get(extJpaSupport.getSoftDeletedPropertyName()), notDeleted);
    this.deletedSpec = (root, query, builder) -> builder.equal(
        root.get(extJpaSupport.getSoftDeletedPropertyName()), deleted);
  }

  private static <T> Collection<T> toCollection(Iterable<T> ts) {

    if (ts instanceof Collection) {
      return (Collection<T>) ts;
    }

    List<T> tCollection = new ArrayList<>();
    for (T t : ts) {
      tCollection.add(t);
    }
    return tCollection;
  }

  private <S extends T> boolean isNew(S entity, boolean dynamicSave) {
    Class<ID> idType = entityInformation.getIdType();
    if (String.class.equals(idType)) {
      ID id = entityInformation.getId(entity);
      if ("".equals(id)) {
        new DirectFieldAccessFallbackBeanWrapper(entity).setPropertyValue(
            entityInformation.getIdAttribute().getName(), null);
        return true;
      } else {
        return entityIsNew(entity, dynamicSave);
      }
    } else {
      return entityIsNew(entity, dynamicSave);
    }
  }

  private <S extends T> boolean entityIsNew(S entity, boolean dynamicSave) {
    if (dynamicSave && entityInformation instanceof JpaMetamodelEntityInformation) {
      ID id = entityInformation.getId(entity);
      Class<ID> idType = entityInformation.getIdType();

      if (!idType.isPrimitive()) {
        return id == null;
      }

      if (id instanceof Number) {
        return ((Number) id).longValue() == 0L;
      }

      throw new IllegalArgumentException(
          String.format("Unsupported primitive id type %s!", idType));
    } else {
      return entityInformation.isNew(entity);
    }
  }

  @Override
  public EntityManager getEntityManager() {
    return em;
  }


  private boolean mdcPutId(String id) {
    if (MDC.get("id") == null) {
      MDC.put("id", entityInformation.getEntityName() + id);
      return true;
    } else {
      return false;
    }
  }

  private void cleanMdc(boolean mdc) {
    if (mdc) {
      MDC.remove("id");
    }
  }

  //--------------------------------------------

  @Override
  public void deleteById(ID id) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".deleteById");

      Assert.notNull(id, "The given id must not be null!");
      findById(id).ifPresent(this::delete);
    } finally {
      cleanMdc(mdc);
    }
  }


  @Override
  public void deleteAll(Iterable<? extends T> entities) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".deleteAll");
      super.deleteAll(entities);
    } finally {
      cleanMdc(mdc);
    }
  }


  @Override
  public void deleteAll() {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".deleteAll");
      super.deleteAll();
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public <S extends T> S saveAndFlush(S entity) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".saveAndFlush");
      return super.saveAndFlush(entity);
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public <S extends T> List<S> saveAll(Iterable<S> entities) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".saveAll");
      return super.saveAll(entities);
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public <S extends T> List<S> saveAllAndFlush(Iterable<S> entities) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".saveAllAndFlush");
      return super.saveAllAndFlush(entities);
    } finally {
      cleanMdc(mdc);
    }
  }

  @Deprecated
  @Override
  public void deleteInBatch(Iterable<T> entities) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".deleteInBatch");
      super.deleteInBatch(entities);
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  @Transactional
  public <S extends T> S save(S entity) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".save");
      if (extJpaSupport.supportSoftDeleted() && !extJpaSupport.softDeletedSeted(entity)) {
        extJpaSupport.setUnSoftDeleted(entity);
      }
      if (isNew(entity, false)) {
        em.persist(entity);
        return entity;
      } else {
        return em.merge(entity);
      }
    } finally {
      cleanMdc(mdc);
    }
  }


  @Transactional
  @Override
  public <S extends T> int save(S s, Specification<T> spec) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".save");
      CriteriaBuilder builder = em.getCriteriaBuilder();
      Class<T> domainClass = getDomainClass();
      CriteriaUpdate<T> criteriaUpdate = builder.createCriteriaUpdate(domainClass);
      Root<T> root = criteriaUpdate.from(domainClass);
      if (extJpaSupport.supportSoftDeleted()) {
        spec = spec == null ? notDeletedSpec : spec.and(notDeletedSpec);
      }
      Predicate predicate = spec.toPredicate(root, builder.createQuery(), builder);
      if (predicate != null) {
        criteriaUpdate.where(predicate);
      }
      DirectFieldAccessFallbackBeanWrapper beanWrapper = new DirectFieldAccessFallbackBeanWrapper(
          s);
      for (SingularAttribute<? super T, ?> attribute : root.getModel().getSingularAttributes()) {
        String attributeName = attribute.getName();
        Object attributeValue = beanWrapper.getPropertyValue(attributeName);
        if (attributeValue == null) {
          continue;
        }
        criteriaUpdate.set(attributeName, attributeValue);
      }
      String lastModifiedDatePropertyName = extJpaSupport.getLastModifiedDatePropertyName();
      if (lastModifiedDatePropertyName != null) {
        criteriaUpdate.set(lastModifiedDatePropertyName,
            extJpaSupport.getLastModifiedDateNowValue());
      }
      String versionPropertyName = extJpaSupport.getVersionPropertyName();
      if (versionPropertyName != null) {
        Object versionIncValue = extJpaSupport.getVersionIncValue();
        if (versionIncValue instanceof Number) {
          Path<Number> path = root.get(versionPropertyName);
          criteriaUpdate.set(path, builder.sum(path, (Number) versionIncValue));
        } else {
          criteriaUpdate.set(versionPropertyName, versionIncValue);
        }
      }

      int affected = em.createQuery(criteriaUpdate).executeUpdate();
      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("{} row affected", affected);
      }
      em.flush();
      return affected;
    } finally {
      cleanMdc(mdc);
    }
  }

  @Deprecated
  @Transactional
  @Override
  public <S extends T> S dynamicSave(S entity) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".dynamicSave");
      if (extJpaSupport.supportSoftDeleted() && !extJpaSupport.softDeletedSeted(entity)) {
        extJpaSupport.setUnSoftDeleted(entity);
      }
      if (isNew(entity, true)) {
        em.persist(entity);
        return entity;
      } else {
        Optional<T> optional = findById(entityInformation.getId(entity));
        if (optional.isPresent()) {
          T exist = optional.get();
          BeanUtil.nullPropertySetFrom(entity, exist);
          return em.merge(entity);
        } else {
          em.persist(entity);
          return entity;
        }
      }
    } finally {
      cleanMdc(mdc);
    }
  }


  @Transactional
  @Override
  public void delete(T entity) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".delete");
      if (extJpaSupport.supportSoftDeleted()) {
        extJpaSupport.setSoftDeleted(entity);
        em.merge(entity);
      } else {
        super.delete(entity);
      }
    } finally {
      cleanMdc(mdc);
    }
  }

  @Transactional
  @Override
  public int delete(Specification<T> spec) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".delete");
      if (extJpaSupport.supportSoftDeleted()) {
        return softDelete(spec);
      } else {
        return hardDelete(spec);
      }
    } finally {
      cleanMdc(mdc);
    }
  }

  private int softDelete(Specification<T> spec) {
    CriteriaBuilder builder = em.getCriteriaBuilder();
    Class<T> domainClass = getDomainClass();
    CriteriaUpdate<T> criteriaUpdate = builder.createCriteriaUpdate(domainClass);
    Root<T> root = criteriaUpdate.from(domainClass);
    spec = spec == null ? notDeletedSpec : spec.and(notDeletedSpec);
    Predicate predicate = spec.toPredicate(root, builder.createQuery(), builder);
    if (predicate != null) {
      criteriaUpdate.where(predicate);
    }
    criteriaUpdate.set(root.get(extJpaSupport.getSoftDeletedPropertyName()), deleted);
    int affected = em.createQuery(criteriaUpdate).executeUpdate();
    if (sqlLog.isDebugEnabled()) {
      sqlLog.debug("{} row affected", affected);
    }
    em.flush();
    return affected;
  }

  private int hardDelete(Specification<T> spec) {
    CriteriaBuilder builder = em.getCriteriaBuilder();
    Class<T> domainClass = getDomainClass();

    CriteriaDelete<T> criteriaDelete = builder.createCriteriaDelete(domainClass);
    Root<T> root = criteriaDelete.from(domainClass);
    if (spec != null) {
      Predicate predicate = spec.toPredicate(root, builder.createQuery(), builder);
      if (predicate != null) {
        criteriaDelete.where(predicate);
      }
    }
    int affected = em.createQuery(criteriaDelete).executeUpdate();
    if (sqlLog.isDebugEnabled()) {
      sqlLog.debug("{} row affected", affected);
    }
    em.flush();
    return affected;
  }

  @Transactional
  @Override
  public void deleteAllById(Iterable<? extends ID> ids) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".deleteAllById");
      delete((root, query, builder) ->
          root.get(entityInformation.getIdAttribute()).in(toCollection(ids)));
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public void deleteAllByIdInBatch(Iterable<ID> ids) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".deleteAllByIdInBatch");
      if (extJpaSupport.supportSoftDeleted()) {
        softDelete((root, query, builder) ->
            root.get(entityInformation.getIdAttribute()).in(toCollection(ids)));
      } else {
        super.deleteAllByIdInBatch(ids);
      }
    } finally {
      cleanMdc(mdc);
    }
  }

  @Transactional
  @Override
  public void deleteAllInBatch(Iterable<T> entities) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".deleteAllInBatch");
      if (extJpaSupport.supportSoftDeleted()) {
        Assert.notNull(entities, "The given Iterable of entities not be null!");
        if (!entities.iterator().hasNext()) {
          return;
        }

        String softDeleteName = extJpaSupport.getSoftDeletedPropertyName();
        String oldName = "old" + softDeleteName;
        String queryString = String
            .format(SOFT_DELETE_ALL_QUERY_STRING, entityInformation.getEntityName(),
                softDeleteName,
                softDeleteName, softDeleteName, oldName);

        Iterator<T> iterator = entities.iterator();

        if (iterator.hasNext()) {
          String alias = "e";
          StringBuilder builder = new StringBuilder(queryString);
          builder.append(" and (");

          int i = 0;

          while (iterator.hasNext()) {

            iterator.next();

            builder.append(String.format(" %s = ?%d", alias, ++i));

            if (iterator.hasNext()) {
              builder.append(" or");
            }
          }
          builder.append(" )");
          Query query = em.createQuery(builder.toString());

          iterator = entities.iterator();
          i = 0;
          query.setParameter(softDeleteName, deleted);
          query.setParameter(oldName, notDeleted);
          while (iterator.hasNext()) {
            query.setParameter(++i, iterator.next());
          }
          int affected = query.executeUpdate();
          if (sqlLog.isDebugEnabled()) {
            sqlLog.debug("{} row affected", affected);
          }
        }
      } else {
        super.deleteAllInBatch(entities);
      }
    } finally {
      cleanMdc(mdc);
    }
  }

  @Transactional
  @Override
  public void deleteAllInBatch() {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".deleteAllInBatch");
      if (extJpaSupport.supportSoftDeleted()) {
        String softDeleteName = extJpaSupport.getSoftDeletedPropertyName();
        String oldName = "old" + softDeleteName;
        int affected = em.createQuery(String
                .format(SOFT_DELETE_ALL_QUERY_STRING, entityInformation.getEntityName(),
                    softDeleteName, softDeleteName, softDeleteName, oldName))
            .setParameter(softDeleteName, deleted)
            .setParameter(oldName, notDeleted)
            .executeUpdate();
        if (sqlLog.isDebugEnabled()) {
          sqlLog.debug("{} row affected", affected);
        }
      } else {
        super.deleteAllInBatch();
      }
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public Optional<T> findById(ID id) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findById");
      if (extJpaSupport.supportSoftDeleted()) {
        Specification<T> spec = (root, query, builder) -> builder.equal(
            root.get(entityInformation.getIdAttribute()), id);
        spec = spec.and(notDeletedSpec);
        return super.findOne(spec);
      } else {
        return super.findById(id);
      }
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public Optional<T> findHardById(ID id) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findHardById");
      return super.findById(id);
    } finally {
      cleanMdc(mdc);
    }
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public T getOne(ID id) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".getOne");
      return getById(id);
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public T getById(ID id) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".getById");
      if (extJpaSupport.supportSoftDeleted()) {
        Specification<T> spec = (root, query, builder) -> builder.equal(
            root.get(entityInformation.getIdAttribute()), id);
        spec = spec.and(notDeletedSpec);
        return super.findOne(spec).orElse(null);
      } else {
        return super.getById(id);
      }
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public boolean existsById(ID id) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".existsById");
      if (extJpaSupport.supportSoftDeleted()) {
        Specification<T> spec = (root, query, builder) -> builder.equal(
            root.get(entityInformation.getIdAttribute()), id);
        spec = spec.and(notDeletedSpec);
        return super.count(spec) > 0;
      } else {
        return super.existsById(id);
      }
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public List<T> findAll() {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAll");
      List<T> result;
      if (extJpaSupport.supportSoftDeleted()) {
        result = super.findAll(notDeletedSpec);
      } else {
        result = super.findAll();
      }
      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("{} rows retrieved", result.size());
      }
      return result;
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public List<T> findAllById(Iterable<ID> ids) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAllById");
      Specification<T> spec = (root, query, builder) ->
          root.get(entityInformation.getIdAttribute()).in(toCollection(ids));
      if (extJpaSupport.supportSoftDeleted()) {
        spec = spec.and(notDeletedSpec);
      }
      List<T> all = super.findAll(spec);
      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("{} rows retrieved", all.size());
      }
      return all;
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public List<T> findAll(Sort sort) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAll");
      List<T> result;
      if (extJpaSupport.supportSoftDeleted()) {
        result = super.findAll(notDeletedSpec, sort);
      } else {
        result = super.findAll(sort);
      }
      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("{} rows retrieved", result.size());
      }
      return result;
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public Page<T> findAll(Pageable pageable) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAll");
      Page<T> result;
      if (extJpaSupport.supportSoftDeleted()) {
        result = super.findAll(notDeletedSpec, pageable);
      } else {
        result = super.findAll(pageable);
      }

      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("total: {} rows", result.getTotalElements());
        sqlLog.debug("{} rows retrieved", result.getContent().size());
      }
      return result;
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public List<T> findAll(int size) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAll");
      Specification<T> spec = null;
      if (extJpaSupport.supportSoftDeleted()) {
        spec = notDeletedSpec;
      }
      return findUnpaged(spec, PageRequest.of(0, size));
    } finally {
      cleanMdc(mdc);
    }
  }


  @Override
  public List<T> findAll(int size, Sort sort) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAll");
      Specification<T> spec = null;
      if (extJpaSupport.supportSoftDeleted()) {
        spec = notDeletedSpec;
      }
      return findUnpaged(spec, PageRequest.of(0, size, sort));
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public long count(Specification<T> spec) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".count");
      if (extJpaSupport.supportSoftDeleted()) {
        spec = spec == null ? notDeletedSpec : spec.and(notDeletedSpec);
      }
      long count = super.count(spec);
      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("total: {} rows", count);
      }
      return count;
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public long countHard(Specification<T> spec) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".count");
      long count = super.count(spec);
      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("total: {} rows", count);
      }
      return count;
    } finally {
      cleanMdc(mdc);
    }
  }


  @Override
  public boolean exists(Specification<T> spec) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".exists");
      return count(spec) > 0;
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public boolean existsHard(Specification<T> spec) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".exists");
      return countHard(spec) > 0;
    } finally {
      cleanMdc(mdc);
    }
  }


  @Override
  public Optional<T> findFirst(Sort sort) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findFirst");
      Specification<T> spec = null;
      if (extJpaSupport.supportSoftDeleted()) {
        spec = notDeletedSpec;
      }
      return findUnpaged(spec, PageRequest.of(0, 1, sort)).stream().findFirst();
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public Optional<T> findFirst(Specification<T> spec) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findFirst");
      if (extJpaSupport.supportSoftDeleted()) {
        spec = spec == null ? notDeletedSpec : spec.and(notDeletedSpec);
      }
      return findUnpaged(spec, PageRequest.of(0, 1)).stream().findFirst();
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public Optional<T> findOne(Specification<T> spec) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findOne");
      if (extJpaSupport.supportSoftDeleted()) {
        spec = spec == null ? notDeletedSpec : spec.and(notDeletedSpec);
      }
      return super.findOne(spec);
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public List<T> findAll(Specification<T> spec) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAll");
      if (extJpaSupport.supportSoftDeleted()) {
        spec = spec == null ? notDeletedSpec : spec.and(notDeletedSpec);
      }
      List<T> all = super.findAll(spec);
      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("{} rows retrieved", all.size());
      }
      return all;
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public List<T> findAll(Specification<T> spec, int size) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAll");
      if (extJpaSupport.supportSoftDeleted()) {
        spec = spec == null ? notDeletedSpec : spec.and(notDeletedSpec);
      }
      return findUnpaged(spec, PageRequest.of(0, size));
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public List<T> findAll(Specification<T> spec, int size, Sort sort) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAll");
      if (extJpaSupport.supportSoftDeleted()) {
        spec = spec == null ? notDeletedSpec : spec.and(notDeletedSpec);
      }
      return findUnpaged(spec, PageRequest.of(0, size, sort));
    } finally {
      cleanMdc(mdc);
    }
  }


  @NotNull
  private List<T> findUnpaged(Specification<T> spec, PageRequest pageable) {
    TypedQuery<T> query = getQuery(spec, pageable);
    query.setFirstResult((int) pageable.getOffset());
    query.setMaxResults(pageable.getPageSize());
    List<T> content = query.getResultList();
    if (sqlLog.isDebugEnabled()) {
      sqlLog.debug("{} rows retrieved", content.size());
    }
    return new PageableList<>(content, pageable,
        Math.min(pageable.getPageSize(), content.size()));
  }

  @Override
  public Page<T> findHardAll(Specification<T> spec, Pageable pageable) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findHardAll");
      Page<T> all = super.findAll(spec, pageable);
      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("total: {} rows", all.getTotalElements());
        sqlLog.debug("{} rows retrieved", all.getContent().size());
      }
      return all;
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public List<T> findHardAllById(Iterable<ID> ids) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findHardAllById");
      List<T> all = super.findAllById(ids);
      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("{} rows retrieved", all.size());
      }
      return all;
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public Page<T> findAll(Specification<T> spec, Pageable pageable) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAll");
      if (extJpaSupport.supportSoftDeleted()) {
        spec = spec == null ? notDeletedSpec : spec.and(notDeletedSpec);
      }
      Page<T> all = super.findAll(spec, pageable);
      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("total: {} rows", all.getTotalElements());
        sqlLog.debug("{} rows retrieved", all.getContent().size());
      }
      return all;
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public List<T> findAll(Specification<T> spec, Sort sort) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAll");
      if (extJpaSupport.supportSoftDeleted()) {
        spec = spec == null ? notDeletedSpec : spec.and(notDeletedSpec);
      }
      List<T> all = super.findAll(spec, sort);
      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("{} rows retrieved", all.size());
      }
      return all;
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public <S extends T> Optional<S> findFirst(Example<S> example) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findFirst");
      return findUnpaged(example, PageRequest.of(0, 1)).stream().findFirst();
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public <S extends T> Optional<S> findOne(Example<S> example) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findOne");
      if (extJpaSupport.supportSoftDeleted()) {
        extJpaSupport.setUnSoftDeleted(example.getProbe());
      }
      return super.findOne(example);
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public <S extends T, R> R findBy(Example<S> example,
      Function<FetchableFluentQuery<S>, R> queryFunction) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findBy");
      if (extJpaSupport.supportSoftDeleted()) {
        extJpaSupport.setUnSoftDeleted(example.getProbe());
      }
      return super.findBy(example, queryFunction);
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public <S extends T> long count(Example<S> example) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".count");
      if (extJpaSupport.supportSoftDeleted()) {
        extJpaSupport.setUnSoftDeleted(example.getProbe());
      }
      long count = super.count(example);
      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("total: {} rows", count);
      }
      return count;
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public <S extends T> boolean exists(Example<S> example) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".exists");
      return count(example) > 0;
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public <S extends T> List<S> findAll(Example<S> example) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAll");
      if (extJpaSupport.supportSoftDeleted()) {
        extJpaSupport.setUnSoftDeleted(example.getProbe());
      }
      List<S> all = super.findAll(example);
      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("{} rows retrieved", all.size());
      }
      return all;
    } finally {
      cleanMdc(mdc);
    }
  }


  @Override
  public <S extends T> List<S> findAll(Example<S> example, int size) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAll");
      return findUnpaged(example, PageRequest.of(0, size));
    } finally {
      cleanMdc(mdc);
    }
  }


  @Override
  public <S extends T> List<S> findAll(Example<S> example, int size, Sort sort) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAll");
      return findUnpaged(example, PageRequest.of(0, size, sort));
    } finally {
      cleanMdc(mdc);
    }
  }


  private <S extends T> List<S> findUnpaged(Example<S> example, PageRequest pageable) {
    if (extJpaSupport.supportSoftDeleted()) {
      extJpaSupport.setUnSoftDeleted(example.getProbe());
    }
    Class<S> probeType = example.getProbeType();
    TypedQuery<S> query = getQuery(new ExampleSpecification<>(example, escapeCharacter), probeType,
        pageable);
    if (pageable.isPaged()) {
      query.setFirstResult((int) pageable.getOffset());
      query.setMaxResults(pageable.getPageSize());
    }
    List<S> content = query.getResultList();
    if (sqlLog.isDebugEnabled()) {
      sqlLog.debug("{} rows retrieved", content.size());
    }
    return new PageableList<>(content, pageable, Math.min(pageable.getPageSize(), content.size()));
  }

  @Override
  public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAll");
      if (extJpaSupport.supportSoftDeleted()) {
        extJpaSupport.setUnSoftDeleted(example.getProbe());
      }
      List<S> all = super.findAll(example, sort);
      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("{} rows retrieved", all.size());
      }
      return all;
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAll");
      if (extJpaSupport.supportSoftDeleted()) {
        extJpaSupport.setUnSoftDeleted(example.getProbe());
      }
      Page<S> all = super.findAll(example, pageable);
      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("total: {} rows", all.getTotalElements());
        sqlLog.debug("{} rows retrieved", all.getContent().size());
      }
      return all;
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public long count() {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".count");
      long count;
      if (extJpaSupport.supportSoftDeleted()) {
        String softDeleteName = extJpaSupport.getSoftDeletedPropertyName();

        String queryString = String.format(COUNT_QUERY_STRING + " WHERE " + EQUALS_CONDITION_STRING,
            provider.getCountQueryPlaceholder(), entityInformation.getEntityName(), "x",
            softDeleteName, softDeleteName);
        count = em.createQuery(queryString, Long.class).setParameter(softDeleteName,
            notDeleted).getSingleResult();
      } else {
        count = super.count();
      }
      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("total: {} rows", count);
      }
      return count;
    } finally {
      cleanMdc(mdc);
    }
  }


  @Transactional
  @Override
  public int cleanRecycleBin() {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".cleanRecycleBin");
      int reslut = 0;
      if (extJpaSupport.supportSoftDeleted()) {
        String softDeleteName = extJpaSupport.getSoftDeletedPropertyName();
        reslut = em.createQuery(String
            .format("delete from %s x where x.%s = :%s", entityInformation.getEntityName(),
                softDeleteName,
                softDeleteName)).setParameter(softDeleteName,
            deleted).executeUpdate();
      }
      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("{} rows affected", reslut);
      }
      return reslut;
    } finally {
      cleanMdc(mdc);
    }
  }

  @Transactional
  @Override
  public void deleteFromRecycleBin(ID id) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".deleteFromRecycleBin");
      if (extJpaSupport.supportSoftDeleted()) {
        Optional<T> entity = findByIdFromRecycleBin(id);
        entity.ifPresent(super::delete);
      }
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public void deleteAllByIdFromRecycleBin(Iterable<ID> ids) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".deleteAllByIdFromRecycleBin");
      if (extJpaSupport.supportSoftDeleted()) {
        Specification<T> spec = (root, query, builder) ->
            root.get(entityInformation.getIdAttribute()).in(toCollection(ids));
        spec = spec.and(deletedSpec);
        hardDelete(spec);
      } else {
        if (sqlLog.isDebugEnabled()) {
          sqlLog.debug("{} rows affected", 0);
        }
      }
    } finally {
      cleanMdc(mdc);
    }
  }

  @Transactional
  @Override
  public void deleteFromRecycleBin(Specification<T> spec) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".deleteFromRecycleBin");
      if (extJpaSupport.supportSoftDeleted()) {
        spec = spec == null ? deletedSpec : spec.and(deletedSpec);
        hardDelete(spec);
      } else {
        if (sqlLog.isDebugEnabled()) {
          sqlLog.debug("{} rows affected", 0);
        }
      }
    } finally {
      cleanMdc(mdc);
    }
  }


  @Override
  public long countRecycleBin() {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".countRecycleBin");
      long count;
      if (extJpaSupport.supportSoftDeleted()) {
        count = super.count(deletedSpec);
      } else {
        count = 0;
      }
      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("total: {} rows", count);
      }
      return count;
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public long countRecycleBin(Specification<T> spec) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".countRecycleBin");
      if (extJpaSupport.supportSoftDeleted()) {
        spec = spec.and(deletedSpec);
      }
      long count = super.count(spec);
      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("total: {} rows", count);
      }
      return count;
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public boolean existsInRecycleBin(Specification<T> spec) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".existsInRecycleBin");
      return countRecycleBin(spec) > 0;
    } finally {
      cleanMdc(mdc);
    }
  }


  @Override
  public Optional<T> findByIdFromRecycleBin(ID id) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findByIdFromRecycleBin");
      if (extJpaSupport.supportSoftDeleted()) {
        Specification<T> spec = (root, query, builder) -> builder.equal(
            root.get(entityInformation.getIdAttribute()), id);
        spec = spec.and(deletedSpec);
        return super.findOne(spec);
      } else {
        return Optional.empty();
      }
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public List<T> findAllByIdFromRecycleBin(Iterable<ID> ids) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAllByIdFromRecycleBin");
      List<T> result;
      if (extJpaSupport.supportSoftDeleted()) {
        Specification<T> spec = (root, query, builder) ->
            root.get(entityInformation.getIdAttribute()).in(toCollection(ids));
        spec = spec.and(deletedSpec);
        result = super.findAll(spec);
      } else {
        result = Collections.emptyList();
      }
      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("{} rows retrieved", result.size());
      }
      return result;
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public Optional<T> findOneFromRecycleBin(Specification<T> spec) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findOneFromRecycleBin");
      if (extJpaSupport.supportSoftDeleted()) {
        spec = spec == null ? deletedSpec : spec.and(deletedSpec);
        return super.findOne(spec);
      } else {
        return Optional.empty();
      }
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public Optional<T> findFirstFromRecycleBin(Specification<T> spec) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findFirstFromRecycleBin");
      return findUnpagedFromRecycleBin(spec, PageRequest.of(0, 1)).stream().findFirst();
    } finally {
      cleanMdc(mdc);
    }
  }


  @Override
  public List<T> findAllFromRecycleBin() {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAllFromRecycleBin");
      List<T> result;
      if (extJpaSupport.supportSoftDeleted()) {
        result = super.findAll(deletedSpec);
      } else {
        result = Collections.emptyList();
      }

      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("{} rows retrieved", result.size());
      }
      return result;
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public List<T> findAllFromRecycleBin(int size) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAllFromRecycleBin");
      return findUnpagedFromRecycleBin(null, PageRequest.of(0, size));
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public List<T> findAllFromRecycleBin(int size, Sort sort) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAllFromRecycleBin");
      return findUnpagedFromRecycleBin(null, PageRequest.of(0, size, sort));
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public Page<T> findAllFromRecycleBin(Pageable pageable) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAllFromRecycleBin");
      Page<T> result;
      if (extJpaSupport.supportSoftDeleted()) {
        result = super.findAll(deletedSpec, pageable);
      } else {
        result = Page.empty(pageable);
      }

      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("total: {} rows", result.getTotalElements());
        sqlLog.debug("{} rows retrieved", result.getContent().size());
      }
      return result;
    } finally {
      cleanMdc(mdc);
    }
  }


  @Override
  public List<T> findAllFromRecycleBin(Sort sort) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAllFromRecycleBin");
      List<T> result;
      if (extJpaSupport.supportSoftDeleted()) {
        result = super.findAll(deletedSpec, sort);
      } else {
        result = Collections.emptyList();
      }

      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("{} rows retrieved", result.size());
      }
      return result;
    } finally {
      cleanMdc(mdc);
    }
  }


  @Override
  public List<T> findAllFromRecycleBin(Specification<T> spec) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAllFromRecycleBin");
      List<T> result;
      if (extJpaSupport.supportSoftDeleted()) {
        spec = spec == null ? deletedSpec : spec.and(deletedSpec);
        result = super.findAll(spec);
      } else {
        result = Collections.emptyList();
      }

      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("{} rows retrieved", result.size());
      }
      return result;
    } finally {
      cleanMdc(mdc);
    }
  }


  @Override
  public List<T> findAllFromRecycleBin(Specification<T> spec, int size) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAllFromRecycleBin");
      return findUnpagedFromRecycleBin(spec, PageRequest.of(0, size));
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public List<T> findAllFromRecycleBin(Specification<T> spec, int size, Sort sort) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAllFromRecycleBin");
      return findUnpagedFromRecycleBin(spec, PageRequest.of(0, size, sort));
    } finally {
      cleanMdc(mdc);
    }
  }

  @NotNull
  private List<T> findUnpagedFromRecycleBin(Specification<T> spec, PageRequest pageable) {
    if (extJpaSupport.supportSoftDeleted()) {
      spec = spec == null ? deletedSpec : spec.and(deletedSpec);
      return findUnpaged(spec, pageable);
    } else {
      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("total: {} rows", 0);
      }
      return new PageableList<>(pageable);
    }
  }


  @Override
  public Page<T> findAllFromRecycleBin(Specification<T> spec, Pageable pageable) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAllFromRecycleBin");
      Page<T> result;
      if (extJpaSupport.supportSoftDeleted()) {
        spec = spec == null ? deletedSpec : spec.and(deletedSpec);
        result = super.findAll(spec, pageable);
      } else {
        result = Page.empty(pageable);
      }

      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("total: {} rows", result.getTotalElements());
        sqlLog.debug("{} rows retrieved", result.getContent().size());
      }
      return result;
    } finally {
      cleanMdc(mdc);
    }
  }

  @Override
  public List<T> findAllFromRecycleBin(Specification<T> spec, Sort sort) {
    boolean mdc = false;
    try {
      mdc = mdcPutId(".findAllFromRecycleBin");
      List<T> result;
      if (extJpaSupport.supportSoftDeleted()) {
        spec = spec == null ? deletedSpec : spec.and(deletedSpec);
        result = super.findAll(spec, sort);
      } else {
        result = Collections.emptyList();
      }

      if (sqlLog.isDebugEnabled()) {
        sqlLog.debug("{} rows retrieved", result.size());
      }
      return result;
    } finally {
      cleanMdc(mdc);
    }
  }

}

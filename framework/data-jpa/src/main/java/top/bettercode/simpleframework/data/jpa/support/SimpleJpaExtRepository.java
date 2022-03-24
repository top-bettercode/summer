package top.bettercode.simpleframework.data.jpa.support;

import static org.springframework.data.jpa.repository.query.QueryUtils.COUNT_QUERY_STRING;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import top.bettercode.simpleframework.data.jpa.JpaExtRepository;
import top.bettercode.simpleframework.data.jpa.config.JpaExtProperties;

/**
 * @author Peter Wu
 */
public class SimpleJpaExtRepository<T, ID> extends
    SimpleJpaRepository<T, ID> implements JpaExtRepository<T, ID> {

  private final Logger log = LoggerFactory.getLogger(SimpleJpaExtRepository.class);
  public static final String SOFT_DELETE_ALL_QUERY_STRING = "update %s e set e.%s = :%s";
  private static final String EQUALS_CONDITION_STRING = "%s.%s = :%s";

  private final JpaEntityInformation<T, ?> entityInformation;
  private final EntityManager em;
  private final PersistenceProvider provider;
  private final SoftDeleteSupport softDelete;
  private final EscapeCharacter escapeCharacter = EscapeCharacter.DEFAULT;
  private final Object notDeleted;
  private final Object deleted;
  private final Specification<T> notDeletedSpec;
  private final Specification<T> deletedSpec;

  public SimpleJpaExtRepository(
      JpaExtProperties jpaExtProperties,
      JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
    super(entityInformation, entityManager);
    this.entityInformation = entityInformation;
    this.em = entityManager;
    this.provider = PersistenceProvider.fromEntityManager(entityManager);
    this.softDelete = new DefaultSoftDeleteSupport(jpaExtProperties, getDomainClass());
    this.notDeleted = softDelete.getFalseValue();
    this.deleted = softDelete.getTrueValue();
    this.notDeletedSpec = (root, query, builder) -> builder.equal(
        root.get(softDelete.getPropertyName()), notDeleted);
    this.deletedSpec = (root, query, builder) -> builder.equal(
        root.get(softDelete.getPropertyName()), deleted);
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

  private <S extends T> boolean isNew(S entity) {
    if (String.class.equals(entityInformation.getIdType())) {
      String id = (String) entityInformation.getId(entity);
      if ("".equals(id)) {
        new DirectFieldAccessFallbackBeanWrapper(entity).setPropertyValue(
            entityInformation.getIdAttribute().getName(), null);
        return true;
      } else {
        return id == null;
      }
    } else {
      return entityInformation.isNew(entity);
    }
  }

  @Override
  public EntityManager getEntityManager() {
    return em;
  }

  @Override
  public <S extends T> S save(S entity) {
    if (isNew(entity)) {
      em.persist(entity);
      return entity;
    } else {
      return em.merge(entity);
    }
  }

  @Transactional
  @Override
  public <S extends T> S dynamicSave(S entity) {
    if (isNew(entity)) {
      em.persist(entity);
      return entity;
    } else {
      @SuppressWarnings("unchecked")
      Optional<T> optional = findById((ID) entityInformation.getId(entity));
      if (optional.isPresent()) {
        T exist = optional.get();
        copyPropertiesIfTargetPropertyNull(exist, entity, true);
        return em.merge(entity);
      } else {
        em.persist(entity);
        return entity;
      }
    }
  }


  @Transactional
  @Override
  public <S extends T> S dynamicBSave(S entity) {
    if (isNew(entity)) {
      em.persist(entity);
      return entity;
    } else {
      @SuppressWarnings("unchecked")
      Optional<T> optional = findById((ID) entityInformation.getId(entity));
      if (optional.isPresent()) {
        T exist = optional.get();
        copyPropertiesIfTargetPropertyNull(exist, entity, false);
        return em.merge(entity);
      } else {
        em.persist(entity);
        return entity;
      }
    }
  }


  private void copyPropertiesIfTargetPropertyNull(Object source, Object target,
      boolean allowEmpty)
      throws BeansException {

    Assert.notNull(source, "Source must not be null");
    Assert.notNull(target, "Target must not be null");

    DirectFieldAccessFallbackBeanWrapper sourceWrapper = new DirectFieldAccessFallbackBeanWrapper(
        source);
    DirectFieldAccessFallbackBeanWrapper targetWrapper = new DirectFieldAccessFallbackBeanWrapper(
        target);
    PropertyDescriptor[] targetPds = targetWrapper.getPropertyDescriptors();

    for (PropertyDescriptor targetPd : targetPds) {
      String propertyName = targetPd.getName();
      if ("class".equals(propertyName)) {
        continue;
      }
      try {
        Object propertyValue = targetWrapper.getPropertyValue(propertyName);
        if (propertyValue != null && (allowEmpty || !"".equals(propertyValue))) {
          continue;
        }
        targetWrapper.setPropertyValue(propertyName, sourceWrapper.getPropertyValue(propertyName));
      } catch (Exception e) {
        log.warn("Could not get or set " + target.getClass() + "." + propertyName, e);
      }
    }
  }

  @Transactional
  @Override
  public void delete(T entity) {
    if (softDelete.support()) {
      softDelete.setSoftDeleted(entity);
      em.merge(entity);
    } else {
      super.delete(entity);
    }
  }

  @Transactional
  @Override
  public void delete(Specification<T> spec) {
    if (softDelete.support()) {
      CriteriaBuilder builder = em.getCriteriaBuilder();
      Class<T> domainClass = getDomainClass();
      CriteriaUpdate<T> criteriaUpdate = builder.createCriteriaUpdate(domainClass);
      Root<T> root = criteriaUpdate.from(domainClass);
      if (spec != null) {
        Predicate predicate = spec.toPredicate(root, builder.createQuery(), builder);
        if (predicate != null) {
          criteriaUpdate.where(predicate);
        }
      }
      criteriaUpdate.set(root.get(softDelete.getPropertyName()), softDelete.getTrueValue());
      em.createQuery(criteriaUpdate).executeUpdate();
      em.flush();
    } else {
      doDelete(spec);
    }
  }

  private void doDelete(Specification<T> spec) {
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
    em.createQuery(criteriaDelete).executeUpdate();
    em.flush();
  }

  @Transactional
  @Override
  public void deleteAllById(Iterable<ID> ids) {
    delete((root, query, builder) ->
        root.get(entityInformation.getIdAttribute()).in(toCollection(ids)));
  }

  @Transactional
  @Override
  public void deleteInBatch(Iterable<T> entities) {
    if (softDelete.support()) {
      Assert.notNull(entities, "The given Iterable of entities not be null!");
      if (!entities.iterator().hasNext()) {
        return;
      }

      String softDeleteName = softDelete.getPropertyName();
      String queryString = String
          .format(SOFT_DELETE_ALL_QUERY_STRING, entityInformation.getEntityName(),
              softDeleteName,
              softDeleteName);

      Iterator<T> iterator = entities.iterator();

      if (iterator.hasNext()) {
        String alias = "e";
        StringBuilder builder = new StringBuilder(queryString);
        builder.append(" where");

        int i = 0;

        while (iterator.hasNext()) {

          iterator.next();

          builder.append(String.format(" %s = ?%d", alias, ++i));

          if (iterator.hasNext()) {
            builder.append(" or");
          }
        }

        Query query = em.createQuery(builder.toString());

        iterator = entities.iterator();
        i = 0;
        query.setParameter(softDeleteName,
            deleted);
        while (iterator.hasNext()) {
          query.setParameter(++i, iterator.next());
        }
        query.executeUpdate();
      }
    } else {
      super.deleteInBatch(entities);
    }
  }

  @Transactional
  @Override
  public void deleteAllInBatch() {
    if (softDelete.support()) {
      String softDeleteName = softDelete.getPropertyName();
      em.createQuery(String
          .format(SOFT_DELETE_ALL_QUERY_STRING, entityInformation.getEntityName(),
              softDeleteName, softDeleteName)).setParameter(softDeleteName,
          deleted).executeUpdate();
    } else {
      super.deleteAllInBatch();
    }
  }

  @Override
  public Optional<T> findById(ID id) {
    if (softDelete.support()) {
      Specification<T> spec = (root, query, builder) -> builder.equal(
          root.get(entityInformation.getIdAttribute()), id);
      spec = spec.and(notDeletedSpec);
      return findOne(spec);
    } else {
      return super.findById(id);
    }
  }

  @Override
  public Optional<T> findHardById(ID id) {
    return super.findById(id);
  }

  @Override
  public T getOne(ID id) {
    T optional = super.getOne(id);
    if (softDelete.support()) {
      if (optional != null && softDelete.isSoftDeleted(optional)) {
        return null;
      } else {
        return optional;
      }
    } else {
      return optional;
    }
  }

  @Override
  public boolean existsById(ID id) {
    if (softDelete.support()) {
      Specification<T> spec = (root, query, builder) -> builder.equal(
          root.get(entityInformation.getIdAttribute()), id);
      spec = spec.and(notDeletedSpec);
      return count(spec) > 0;
    } else {
      return super.existsById(id);
    }
  }

  @Override
  public List<T> findAll() {
    if (softDelete.support()) {
      return super.findAll(notDeletedSpec);
    } else {
      return super.findAll();
    }
  }

  @Override
  public List<T> findAllById(Iterable<ID> ids) {
    Specification<T> spec = (root, query, builder) ->
        root.get(entityInformation.getIdAttribute()).in(toCollection(ids));
    if (softDelete.support()) {
      spec = spec.and(notDeletedSpec);
    }
    return super.findAll(spec);
  }

  @Override
  public List<T> findAll(Sort sort) {
    if (softDelete.support()) {
      return super.findAll(notDeletedSpec, sort);
    } else {
      return super.findAll(sort);
    }
  }

  @Override
  public Page<T> findAll(Pageable pageable) {
    if (softDelete.support()) {
      return super.findAll(notDeletedSpec, pageable);
    } else {
      return super.findAll(pageable);
    }
  }

  @Override
  public List<T> findAll(int size) {
    Specification<T> spec = null;
    if (softDelete.support()) {
      spec = notDeletedSpec;
    }
    return findUnpaged(spec, PageRequest.of(0, size));
  }


  @Override
  public List<T> findAll(int size, Sort sort) {
    Specification<T> spec = null;
    if (softDelete.support()) {
      spec = notDeletedSpec;
    }
    return findUnpaged(spec, PageRequest.of(0, size, sort));
  }

  @Override
  public long count(Specification<T> spec) {
    if (softDelete.support()) {
      spec = spec == null ? notDeletedSpec : spec.and(notDeletedSpec);
    }
    return super.count(spec);
  }

  @Override
  public boolean exists(Specification<T> spec) {
    return count(spec) > 0;
  }

  @Override
  public Optional<T> findFirst(Sort sort) {
    Specification<T> spec = null;
    if (softDelete.support()) {
      spec = notDeletedSpec;
    }
    return findUnpaged(spec, PageRequest.of(0, 1, sort)).stream().findFirst();
  }

  @Override
  public Optional<T> findFirst(Specification<T> spec) {
    if (softDelete.support()) {
      spec = spec == null ? notDeletedSpec : spec.and(notDeletedSpec);
    }
    return findUnpaged(spec, PageRequest.of(0, 1)).stream().findFirst();
  }

  @Override
  public Optional<T> findOne(Specification<T> spec) {
    if (softDelete.support()) {
      spec = spec == null ? notDeletedSpec : spec.and(notDeletedSpec);
    }
    return super.findOne(spec);
  }

  @Override
  public List<T> findAll(Specification<T> spec) {
    if (softDelete.support()) {
      spec = spec == null ? notDeletedSpec : spec.and(notDeletedSpec);
    }
    return super.findAll(spec);
  }

  @Override
  public List<T> findAll(Specification<T> spec, int size) {
    if (softDelete.support()) {
      spec = spec == null ? notDeletedSpec : spec.and(notDeletedSpec);
    }
    return findUnpaged(spec, PageRequest.of(0, size));
  }

  @Override
  public List<T> findAll(Specification<T> spec, int size, Sort sort) {
    if (softDelete.support()) {
      spec = spec == null ? notDeletedSpec : spec.and(notDeletedSpec);
    }
    return findUnpaged(spec, PageRequest.of(0, size, sort));
  }


  @NotNull
  private List<T> findUnpaged(Specification<T> spec, PageRequest pageable) {
    TypedQuery<T> query = getQuery(spec, pageable);
    query.setFirstResult((int) pageable.getOffset());
    query.setMaxResults(pageable.getPageSize());
    List<T> content = query.getResultList();
    return new PageableList<>(content, pageable, Math.min(pageable.getPageSize(), content.size()));
  }


  @Override
  public Page<T> findAll(Specification<T> spec, Pageable pageable) {
    if (softDelete.support()) {
      spec = spec == null ? notDeletedSpec : spec.and(notDeletedSpec);
    }
    return super.findAll(spec, pageable);
  }

  @Override
  public List<T> findAll(Specification<T> spec, Sort sort) {
    if (softDelete.support()) {
      spec = spec == null ? notDeletedSpec : spec.and(notDeletedSpec);
    }
    return super.findAll(spec, sort);
  }

  @Override
  public <S extends T> Optional<S> findFirst(Example<S> example) {
    return findUnpaged(example, PageRequest.of(0, 1)).stream().findFirst();
  }

  @Override
  public <S extends T> Optional<S> findOne(Example<S> example) {
    if (softDelete.support()) {
      softDelete.setUnSoftDeleted(example.getProbe());
    }
    return super.findOne(example);
  }

  @Override
  public <S extends T> long count(Example<S> example) {
    if (softDelete.support()) {
      softDelete.setUnSoftDeleted(example.getProbe());
    }
    return super.count(example);
  }

  @Override
  public <S extends T> boolean exists(Example<S> example) {
    return count(example) > 0;
  }

  @Override
  public <S extends T> List<S> findAll(Example<S> example) {
    if (softDelete.support()) {
      softDelete.setUnSoftDeleted(example.getProbe());
    }
    return super.findAll(example);
  }


  @Override
  public <S extends T> List<S> findAll(Example<S> example, int size) {
    return findUnpaged(example, PageRequest.of(0, size));
  }


  @Override
  public <S extends T> List<S> findAll(Example<S> example, int size, Sort sort) {
    return findUnpaged(example, PageRequest.of(0, size, sort));
  }


  private <S extends T> List<S> findUnpaged(Example<S> example, PageRequest pageable) {
    if (softDelete.support()) {
      softDelete.setUnSoftDeleted(example.getProbe());
    }
    Class<S> probeType = example.getProbeType();
    TypedQuery<S> query = getQuery(new ExampleSpecification<>(example, escapeCharacter), probeType,
        pageable);
    if (pageable.isPaged()) {
      query.setFirstResult((int) pageable.getOffset());
      query.setMaxResults(pageable.getPageSize());
    }
    List<S> content = query.getResultList();
    return new PageableList<>(content, pageable, Math.min(pageable.getPageSize(), content.size()));
  }

  @Override
  public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
    if (softDelete.support()) {
      softDelete.setUnSoftDeleted(example.getProbe());
    }
    return super.findAll(example, sort);
  }

  @Override
  public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
    if (softDelete.support()) {
      softDelete.setUnSoftDeleted(example.getProbe());
    }
    return super.findAll(example, pageable);
  }

  @Override
  public long count() {
    if (softDelete.support()) {
      String softDeleteName = softDelete.getPropertyName();

      String queryString = String.format(COUNT_QUERY_STRING + " WHERE " + EQUALS_CONDITION_STRING,
          provider.getCountQueryPlaceholder(), entityInformation.getEntityName(), "x",
          softDeleteName, softDeleteName);
      return em.createQuery(queryString, Long.class).setParameter(softDeleteName,
          notDeleted).getSingleResult();
    } else {
      return super.count();
    }
  }


  @Transactional
  @Override
  public void cleanRecycleBin() {
    if (softDelete.support()) {
      String softDeleteName = softDelete.getPropertyName();
      em.createQuery(String
          .format("delete from %s x where x.%s = :%s", entityInformation.getEntityName(),
              softDeleteName,
              softDeleteName)).setParameter(softDeleteName,
          deleted).executeUpdate();
    }
  }

  @Transactional
  @Override
  public void deleteFromRecycleBin(ID id) {
    if (softDelete.support()) {
      Optional<T> entity = findByIdFromRecycleBin(id);
      entity.ifPresent(super::delete);
    }
  }

  @Override
  public void deleteAllByIdFromRecycleBin(Iterable<ID> ids) {
    if (softDelete.support()) {
      Specification<T> spec = (root, query, builder) ->
          root.get(entityInformation.getIdAttribute()).in(toCollection(ids));
      spec = spec.and(deletedSpec);
      doDelete(spec);
    }
  }

  @Transactional
  @Override
  public void deleteFromRecycleBin(Specification<T> spec) {
    if (softDelete.support()) {
      spec = spec == null ? deletedSpec : spec.and(deletedSpec);
      doDelete(spec);
    }
  }


  @Override
  public long countRecycleBin() {
    if (softDelete.support()) {
      return super.count(deletedSpec);
    } else {
      return 0;
    }
  }

  @Override
  public long countRecycleBin(Specification<T> spec) {
    if (softDelete.support()) {
      spec = spec.and(deletedSpec);
    }
    return super.count(spec);
  }

  @Override
  public boolean existsInRecycleBin(Specification<T> spec) {
    return countRecycleBin(spec) > 0;
  }


  @Override
  public Optional<T> findByIdFromRecycleBin(ID id) {
    if (softDelete.support()) {
      Optional<T> optional = super.findById(id);
      if (optional.isPresent() && !softDelete.isSoftDeleted(optional.get())) {
        return Optional.empty();
      } else {
        return optional;
      }
    } else {
      return Optional.empty();
    }
  }

  @Override
  public List<T> findAllByIdFromRecycleBin(Iterable<ID> ids) {
    if (softDelete.support()) {
      Specification<T> spec = (root, query, builder) ->
          root.get(entityInformation.getIdAttribute()).in(toCollection(ids));
      spec = spec.and(deletedSpec);
      return super.findAll(spec);
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public Optional<T> findOneFromRecycleBin(Specification<T> spec) {
    if (softDelete.support()) {
      spec = spec == null ? deletedSpec : spec.and(deletedSpec);
      return super.findOne(spec);
    } else {
      return Optional.empty();
    }
  }

  @Override
  public Optional<T> findFirstFromRecycleBin(Specification<T> spec) {
    return findUnpagedFromRecycleBin(spec, PageRequest.of(0, 1)).stream().findFirst();
  }


  @Override
  public List<T> findAllFromRecycleBin() {
    if (softDelete.support()) {
      return super.findAll(deletedSpec);
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public List<T> findAllFromRecycleBin(int size) {
    return findUnpagedFromRecycleBin(null, PageRequest.of(0, size));
  }

  @Override
  public List<T> findAllFromRecycleBin(int size, Sort sort) {
    return findUnpagedFromRecycleBin(null, PageRequest.of(0, size, sort));
  }

  @Override
  public Page<T> findAllFromRecycleBin(Pageable pageable) {
    if (softDelete.support()) {
      return super.findAll(deletedSpec, pageable);
    } else {
      return Page.empty(pageable);
    }
  }


  @Override
  public List<T> findAllFromRecycleBin(Sort sort) {
    if (softDelete.support()) {
      return super.findAll(deletedSpec, sort);
    } else {
      return Collections.emptyList();
    }
  }


  @Override
  public List<T> findAllFromRecycleBin(Specification<T> spec) {
    if (softDelete.support()) {
      spec = spec == null ? deletedSpec : spec.and(deletedSpec);
      return super.findAll(spec);
    } else {
      return Collections.emptyList();
    }
  }


  @Override
  public List<T> findAllFromRecycleBin(Specification<T> spec, int size) {
    return findUnpagedFromRecycleBin(spec, PageRequest.of(0, size));
  }

  @Override
  public List<T> findAllFromRecycleBin(Specification<T> spec, int size, Sort sort) {
    return findUnpagedFromRecycleBin(spec, PageRequest.of(0, size, sort));
  }

  @NotNull
  private List<T> findUnpagedFromRecycleBin(Specification<T> spec, PageRequest pageable) {
    if (softDelete.support()) {
      spec = spec == null ? deletedSpec : spec.and(deletedSpec);
      return findUnpaged(spec, pageable);
    } else {
      return new PageableList<>(pageable);
    }
  }


  @Override
  public Page<T> findAllFromRecycleBin(Specification<T> spec, Pageable pageable) {
    if (softDelete.support()) {
      spec = spec == null ? deletedSpec : spec.and(deletedSpec);
      return super.findAll(spec, pageable);
    } else {
      return Page.empty(pageable);
    }
  }

  @Override
  public List<T> findAllFromRecycleBin(Specification<T> spec, Sort sort) {
    if (softDelete.support()) {
      spec = spec == null ? deletedSpec : spec.and(deletedSpec);
      return super.findAll(spec, sort);
    } else {
      return Collections.emptyList();
    }
  }

}

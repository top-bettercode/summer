package top.bettercode.simpleframework.data.jpa.support;

import static org.springframework.data.jpa.repository.query.QueryUtils.COUNT_QUERY_STRING;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import top.bettercode.simpleframework.data.jpa.JpaExtRepository;
import top.bettercode.simpleframework.data.jpa.config.JpaExtProperties;
import top.bettercode.simpleframework.exception.ResourceNotFoundException;

/**
 * @author Peter Wu
 */
public class SimpleJpaExtRepository<T, ID> extends
    SimpleJpaRepository<T, ID> implements JpaExtRepository<T, ID> {

  public static final String SOFT_DELETE_ALL_QUERY_STRING = "update %s e set e.%s = :%s";
  private static final String EQUALS_CONDITION_STRING = "%s.%s = :%s";

  private final JpaEntityInformation<T, ?> entityInformation;
  private final EntityManager em;
  private final PersistenceProvider provider;
  private final SoftDeleteSupport softDeleteSupport;

  public SimpleJpaExtRepository(
      JpaExtProperties jpaExtProperties,
      JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
    super(entityInformation, entityManager);
    this.entityInformation = entityInformation;
    this.em = entityManager;
    this.provider = PersistenceProvider.fromEntityManager(entityManager);
    this.softDeleteSupport = new DefaultSoftDeleteSupport(jpaExtProperties, getDomainClass());
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


  private <S extends T> Specification<S> getSoftDeleteSpecification(Object value) {
    return (root, query, builder) -> builder
        .equal(root.get(softDeleteSupport.getPropertyName()), value);
  }

  @Transactional
  @Override
  public <S extends T> S dynamicSave(S s) {
    if (entityInformation.isNew(s)) {
      em.persist(s);
      return s;
    } else {
      @SuppressWarnings("unchecked")
      Optional<T> optional = findById((ID) entityInformation.getId(s));
      if (optional.isPresent()) {
        T exist = optional.get();
        copyPropertiesIfTargetPropertyNull(exist, s, true);
        return em.merge(s);
      } else {
        throw new ResourceNotFoundException();
      }
    }
  }


  @Transactional
  @Override
  public <S extends T> S dynamicBSave(S s) {
    if (entityInformation.isNew(s)) {
      em.persist(s);
      return s;
    } else {
      @SuppressWarnings("unchecked")
      Optional<T> optional = findById((ID) entityInformation.getId(s));
      if (optional.isPresent()) {
        T exist = optional.get();
        copyPropertiesIfTargetPropertyNull(exist, s, false);
        return em.merge(s);
      } else {
        throw new ResourceNotFoundException();
      }
    }
  }


  private static void copyPropertiesIfTargetPropertyNull(Object source, Object target,
      boolean allowBlank)
      throws BeansException {

    Assert.notNull(source, "Source must not be null");
    Assert.notNull(target, "Target must not be null");

    Class<?> actualEditable = target.getClass();

    PropertyDescriptor[] targetPds = BeanUtils.getPropertyDescriptors(actualEditable);

    for (PropertyDescriptor targetPd : targetPds) {
      Method targetPdReadMethod = targetPd.getReadMethod();
      if (targetPdReadMethod == null) {
        continue;
      }
      if (!Modifier.isPublic(targetPdReadMethod.getDeclaringClass().getModifiers())) {
        targetPdReadMethod.setAccessible(true);
      }
      try {
        Object invoke = targetPdReadMethod.invoke(target);
        if (invoke != null && (allowBlank || !"".equals(invoke))) {
          continue;
        }
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new FatalBeanException(
            "Could not copy property '" + targetPd.getName() + "' from source to target", e);
      }
      Method writeMethod = targetPd.getWriteMethod();
      if (writeMethod != null) {
        PropertyDescriptor sourcePd = BeanUtils
            .getPropertyDescriptor(source.getClass(), targetPd.getName());
        if (sourcePd != null) {
          Method readMethod = sourcePd.getReadMethod();
          if (readMethod != null &&
              ClassUtils
                  .isAssignable(writeMethod.getParameterTypes()[0], readMethod.getReturnType())) {
            try {
              if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                readMethod.setAccessible(true);
              }
              Object value = readMethod.invoke(source);
              if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                writeMethod.setAccessible(true);
              }
              writeMethod.invoke(target, value);
            } catch (Throwable ex) {
              throw new FatalBeanException(
                  "Could not copy property '" + targetPd.getName() + "' from source to target", ex);
            }
          }
        }
      }
    }
  }

  @Override
  public void delete(T entity) {
    if (softDeleteSupport.support()) {
      softDeleteSupport.setSoftDeleted(entity);
      em.merge(entity);
    } else {
      super.delete(entity);
    }
  }

  @Transactional
  @Override
  public void cleanRecycleBin() {
    if (softDeleteSupport.support()) {
      String softDeleteName = softDeleteSupport.getPropertyName();
      em.createQuery(String
          .format("delete from %s x where x.%s = :%s", entityInformation.getEntityName(),
              softDeleteName,
              softDeleteName)).setParameter(softDeleteName,
          softDeleteSupport.getTrueValue()).executeUpdate();
    }
  }

  @Transactional
  @Override
  public void deleteFromRecycleBin(ID id) {
    if (softDeleteSupport.support()) {
      Optional<T> entity = findByIdFromRecycleBin(id);
      entity.ifPresent(super::delete);
    }
  }

  @Transactional
  @Override
  public void deleteFromRecycleBin(Specification<T> spec) {
    if (softDeleteSupport.support()) {
      Iterable<T> allFromRecycleBin = findAllFromRecycleBin(spec);
      super.deleteInBatch(allFromRecycleBin);
    }
  }

  @Override
  public void deleteInBatch(Iterable<T> entities) {
    if (softDeleteSupport.support()) {
      Assert.notNull(entities, "The given Iterable of entities not be null!");
      if (!entities.iterator().hasNext()) {
        return;
      }

      String softDeleteName = softDeleteSupport.getPropertyName();
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
            softDeleteSupport.getTrueValue());
        while (iterator.hasNext()) {
          query.setParameter(++i, iterator.next());
        }
        query.executeUpdate();
      }
    } else {
      super.deleteInBatch(entities);
    }
  }

  @Override
  public void deleteAllInBatch() {
    if (softDeleteSupport.support()) {
      String softDeleteName = softDeleteSupport.getPropertyName();
      em.createQuery(String
          .format(SOFT_DELETE_ALL_QUERY_STRING, entityInformation.getEntityName(),
              softDeleteName,
              softDeleteName)).setParameter(softDeleteName,
          softDeleteSupport.getTrueValue()).executeUpdate();
    } else {
      super.deleteAllInBatch();
    }
  }

  @Override
  public Optional<T> findById(ID id) {
    Optional<T> optional = super.findById(id);
    if (softDeleteSupport.support()) {
      if (optional.isPresent() && softDeleteSupport.isSoftDeleted(optional.get())) {
        return Optional.empty();
      } else {
        return optional;
      }
    } else {
      return optional;
    }
  }

  @Override
  public Optional<T> findHardById(ID id) {
    return super.findById(id);
  }

  @Override
  public T getOne(ID id) {
    T optional = super.getOne(id);
    if (softDeleteSupport.support()) {
      if (optional != null && softDeleteSupport.isSoftDeleted(optional)) {
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
    if (softDeleteSupport.support()) {
      Assert.notNull(id, "The given id must not be null!");

      if (entityInformation.getIdAttribute() == null) {
        return findById(id).isPresent();
      }

      String softDeleteName = softDeleteSupport.getPropertyName();

      String placeholder = provider.getCountQueryPlaceholder();
      String entityName = entityInformation.getEntityName();
      Iterable<String> idAttributeNames = entityInformation.getIdAttributeNames();
      String existsQuery =
          QueryUtils.getExistsQueryString(entityName, placeholder, idAttributeNames) + " AND "
              + String.format(EQUALS_CONDITION_STRING, "x", softDeleteName, softDeleteName);

      TypedQuery<Long> query = em.createQuery(existsQuery, Long.class);

      if (!entityInformation.hasCompositeId()) {
        query.setParameter(idAttributeNames.iterator().next(), id);
        query.setParameter(softDeleteName,
            softDeleteSupport.getFalseValue());

        return query.getSingleResult() == 1L;
      }

      for (String idAttributeName : idAttributeNames) {

        Object idAttributeValue = entityInformation
            .getCompositeIdAttributeValue(id, idAttributeName);

        boolean complexIdParameterValueDiscovered = idAttributeValue != null
            && !query.getParameter(idAttributeName).getParameterType()
            .isAssignableFrom(idAttributeValue.getClass());

        if (complexIdParameterValueDiscovered) {

          // fall-back to findById(id) which does the proper mapping for the parameter.
          return findById(id).isPresent();
        }

        query.setParameter(idAttributeName, idAttributeValue);
      }
      query.setParameter(softDeleteName,
          softDeleteSupport.getFalseValue());

      return query.getSingleResult() == 1L;
    } else {
      return super.existsById(id);
    }
  }

  @Override
  public List<T> findAll() {
    if (softDeleteSupport.support()) {
      return super.findAll(getSoftDeleteSpecification(softDeleteSupport.getFalseValue()));
    } else {
      return super.findAll();
    }
  }

  @Override
  public List<T> findAllById(Iterable<ID> ids) {
    if (softDeleteSupport.support()) {

      Assert.notNull(ids, "The given Iterable of Id's must not be null!");

      if (!ids.iterator().hasNext()) {
        return Collections.emptyList();
      }

      if (entityInformation.hasCompositeId()) {

        List<T> results = new ArrayList<>();

        for (ID id : ids) {
          findById(id).ifPresent(results::add);
        }

        return results;
      }

      Collection<ID> idCollection = toCollection(ids);

      ByIdsSpecification<T> specification = new ByIdsSpecification<>(entityInformation);
      Specification<T> spec = getSoftDeleteSpecification(softDeleteSupport.getFalseValue())
          .and(specification);
      TypedQuery<T> query = getQuery(spec, Sort.unsorted());

      return query.setParameter(specification.parameter, idCollection).getResultList();
    } else {
      return super.findAllById(ids);
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static final class ByIdsSpecification<T> implements Specification<T> {

    private static final long serialVersionUID = 1L;

    private final JpaEntityInformation<T, ?> entityInformation;

    @Nullable
    ParameterExpression<Collection<?>> parameter;

    ByIdsSpecification(JpaEntityInformation<T, ?> entityInformation) {
      this.entityInformation = entityInformation;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.jpa.domain.Specification#toPredicate(javax.persistence.criteria.Root, javax.persistence.criteria.CriteriaQuery, javax.persistence.criteria.CriteriaBuilder)
     */
    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

      Path<?> path = root.get(entityInformation.getIdAttribute());
      parameter = (ParameterExpression<Collection<?>>) (ParameterExpression) cb.parameter(
          Collection.class);
      return path.in(parameter);
    }
  }


  @Override
  public List<T> findAll(Sort sort) {
    if (softDeleteSupport.support()) {
      return super.findAll(getSoftDeleteSpecification(softDeleteSupport.getFalseValue()), sort);
    } else {
      return super.findAll(sort);
    }
  }

  @Override
  public Page<T> findAll(Pageable pageable) {
    if (softDeleteSupport.support()) {
      return super.findAll(getSoftDeleteSpecification(softDeleteSupport.getFalseValue()), pageable);
    } else {
      return super.findAll(pageable);
    }
  }


  @Override
  public long count(Specification<T> spec) {
    if (softDeleteSupport.support()) {
      spec = spec.and(getSoftDeleteSpecification(softDeleteSupport.getFalseValue()));
    }
    return super.count(spec);
  }

  @Override
  public boolean exists(Specification<T> spec) {
    return count(spec) > 0;
  }

  @Override
  public Optional<T> findOne(Specification<T> spec) {
    if (softDeleteSupport.support()) {
      spec = spec.and(getSoftDeleteSpecification(softDeleteSupport.getFalseValue()));
    }
    return super.findOne(spec);
  }

  @Override
  public List<T> findAll(Specification<T> spec) {
    if (softDeleteSupport.support()) {
      spec = spec.and(getSoftDeleteSpecification(softDeleteSupport.getFalseValue()));
    }
    return super.findAll(spec);
  }

  @Override
  public Page<T> findAll(Specification<T> spec, Pageable pageable) {
    if (softDeleteSupport.support()) {
      spec = spec.and(getSoftDeleteSpecification(softDeleteSupport.getFalseValue()));
    }
    return super.findAll(spec, pageable);
  }

  @Override
  public List<T> findAll(Specification<T> spec, Sort sort) {
    if (softDeleteSupport.support()) {
      spec = spec.and(getSoftDeleteSpecification(softDeleteSupport.getFalseValue()));
    }
    return super.findAll(spec, sort);
  }

  @Override
  public <S extends T> Optional<S> findOne(Example<S> example) {
    if (softDeleteSupport.support()) {
      softDeleteSupport.setUnSoftDeleted(example.getProbe());
    }
    return super.findOne(example);
  }

  @Override
  public <S extends T> long count(Example<S> example) {
    if (softDeleteSupport.support()) {
      softDeleteSupport.setUnSoftDeleted(example.getProbe());
    }
    return super.count(example);
  }

  @Override
  public <S extends T> boolean exists(Example<S> example) {
    return count(example) > 0;
  }

  @Override
  public <S extends T> List<S> findAll(Example<S> example) {
    if (softDeleteSupport.support()) {
      softDeleteSupport.setUnSoftDeleted(example.getProbe());
    }
    return super.findAll(example);
  }

  @Override
  public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
    if (softDeleteSupport.support()) {
      softDeleteSupport.setUnSoftDeleted(example.getProbe());
    }
    return super.findAll(example, sort);
  }

  @Override
  public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
    if (softDeleteSupport.support()) {
      softDeleteSupport.setUnSoftDeleted(example.getProbe());
    }
    return super.findAll(example, pageable);
  }

  @Override
  public long count() {
    if (softDeleteSupport.support()) {
      String softDeleteName = softDeleteSupport.getPropertyName();

      String queryString = String.format(COUNT_QUERY_STRING + " WHERE " + EQUALS_CONDITION_STRING,
          provider.getCountQueryPlaceholder(), entityInformation.getEntityName(), "x",
          softDeleteName, softDeleteName);
      return em.createQuery(queryString, Long.class).setParameter(softDeleteName,
          softDeleteSupport.getFalseValue()).getSingleResult();
    } else {
      return super.count();
    }
  }

  @Override
  public List<T> findAllFromRecycleBin() {
    if (softDeleteSupport.support()) {
      return super.findAll(getSoftDeleteSpecification(softDeleteSupport.getTrueValue()));
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public Optional<T> findByIdFromRecycleBin(ID id) {
    if (softDeleteSupport.support()) {
      Optional<T> optional = super.findById(id);
      if (optional.isPresent() && !softDeleteSupport.isSoftDeleted(optional.get())) {
        return Optional.empty();
      } else {
        return optional;
      }
    } else {
      return Optional.empty();
    }
  }


  @Override
  public long countRecycleBin() {
    if (softDeleteSupport.support()) {
      return super.count(getSoftDeleteSpecification(softDeleteSupport.getTrueValue()));
    } else {
      return 0;
    }
  }


  @Override
  public Optional<T> findOneFromRecycleBin(Specification<T> spec) {
    if (softDeleteSupport.support()) {
      spec = spec.and(getSoftDeleteSpecification(softDeleteSupport.getTrueValue()));
      return super.findOne(spec);
    } else {
      return Optional.empty();
    }
  }

  @Override
  public List<T> findAllFromRecycleBin(Specification<T> spec) {
    if (softDeleteSupport.support()) {
      spec = spec.and(getSoftDeleteSpecification(softDeleteSupport.getTrueValue()));
    }
    return super.findAll(spec);
  }

  @Override
  public Page<T> findAllFromRecycleBin(Specification<T> spec, Pageable pageable) {
    if (softDeleteSupport.support()) {
      spec = spec.and(getSoftDeleteSpecification(softDeleteSupport.getTrueValue()));
    }
    return super.findAll(spec, pageable);
  }

  @Override
  public List<T> findAllFromRecycleBin(Specification<T> spec, Sort sort) {
    if (softDeleteSupport.support()) {
      spec = spec.and(getSoftDeleteSpecification(softDeleteSupport.getTrueValue()));
    }
    return super.findAll(spec, sort);
  }

  @Override
  public long countRecycleBin(Specification<T> spec) {
    if (softDeleteSupport.support()) {
      spec = spec.and(getSoftDeleteSpecification(softDeleteSupport.getTrueValue()));
    }
    return super.count(spec);
  }

  @Override
  public boolean existsInRecycleBin(Specification<T> spec) {
    return countRecycleBin(spec) > 0;
  }
}

package top.bettercode.simpleframework.data.jpa.support;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * @author Peter Wu
 */
public class PageableList<T> extends PageImpl<T> implements List<T> {

  private static final long serialVersionUID = 1L;

  public PageableList(List<T> content, Pageable pageable,
      long total) {
    super(content, pageable, total);
  }

  public PageableList(List<T> content) {
    super(content);
  }

  public PageableList(Page<T> page) {
    this(page.getContent(), page.getPageable(), page.getTotalElements());
  }

  public PageableList(Pageable pageable) {
    this(Collections.emptyList(), pageable, 0);
  }

  public int size() {
    return getContent().size();
  }

  @Override
  public boolean isEmpty() {
    return getContent().isEmpty();
  }

  public boolean contains(Object o) {
    return getContent().contains(o);
  }

  public Iterator<T> iterator() {
    return getContent().iterator();
  }

  public Object[] toArray() {
    return getContent().toArray();
  }

  public <T1> T1[] toArray(@NotNull T1[] a) {
    return getContent().toArray(a);
  }

  public boolean add(T t) {
    return getContent().add(t);
  }

  public boolean remove(Object o) {
    return getContent().remove(o);
  }

  public boolean containsAll(@NotNull Collection<?> c) {
    return new HashSet<>(getContent()).containsAll(c);
  }

  public boolean addAll(@NotNull Collection<? extends T> c) {
    return getContent().addAll(c);
  }

  public boolean addAll(int index,
      @NotNull Collection<? extends T> c) {
    return getContent().addAll(index, c);
  }

  public boolean removeAll(@NotNull Collection<?> c) {
    return getContent().removeAll(c);
  }

  public boolean retainAll(@NotNull Collection<?> c) {
    return getContent().retainAll(c);
  }

  public void replaceAll(UnaryOperator<T> operator) {
    getContent().replaceAll(operator);
  }

  public void sort(Comparator<? super T> c) {
    getContent().sort(c);
  }

  public void clear() {
    getContent().clear();
  }

  public boolean equals(Object o) {
    return getContent().equals(o);
  }

  public int hashCode() {
    return getContent().hashCode();
  }

  public T get(int index) {
    return getContent().get(index);
  }

  public T set(int index, T element) {
    return getContent().set(index, element);
  }

  public void add(int index, T element) {
    getContent().add(index, element);
  }

  public T remove(int index) {
    return getContent().remove(index);
  }

  public int indexOf(Object o) {
    return getContent().indexOf(o);
  }

  public int lastIndexOf(Object o) {
    return getContent().lastIndexOf(o);
  }

  public ListIterator<T> listIterator() {
    return getContent().listIterator();
  }

  public ListIterator<T> listIterator(int index) {
    return getContent().listIterator(index);
  }

  public List<T> subList(int fromIndex, int toIndex) {
    return getContent().subList(fromIndex, toIndex);
  }

  @Override
  public Spliterator<T> spliterator() {
    return getContent().spliterator();
  }

  public boolean removeIf(Predicate<? super T> filter) {
    return getContent().removeIf(filter);
  }

  @NotNull
  @Override
  public Stream<T> stream() {
    return getContent().stream();
  }

  public Stream<T> parallelStream() {
    return getContent().parallelStream();
  }

  @Override
  public void forEach(Consumer<? super T> action) {
    getContent().forEach(action);
  }
}

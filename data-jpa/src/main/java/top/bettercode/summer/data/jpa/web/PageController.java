package top.bettercode.summer.data.jpa.web;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import top.bettercode.summer.web.BaseController;
import top.bettercode.summer.web.PagedResources;
import top.bettercode.summer.web.PagedResources.PageMetadata;
import top.bettercode.summer.web.RespExtra;

/**
 * @author Peter Wu
 */
public class PageController extends BaseController {

  @Autowired
  private SpringDataWebProperties properties;

  @Override
  protected RespExtra<?> of(Object object) {
    return super.of(pagedObject(object));
  }

  @Override
  protected ResponseEntity<?> ok(Object object) {
    if (object instanceof Page) {
      return super.ok(pagedResources((Page<?>) object));
    } else {
      return super.ok(object);
    }
  }

  protected <T, R> ResponseEntity<?> ok(Page<T> object, Function<? super T, ? extends R> mapper) {
    int number =
        properties.getPageable().isOneIndexedParameters() ? object.getNumber() + 1
            : object.getNumber();
    List<T> content = object.getContent();
    return super.ok(
        new PagedResources<>(new PageMetadata(number, object.getSize(), object.getTotalPages(),
            object.getTotalElements()),
            content.stream().map(mapper).collect(Collectors.toList())));
  }

  protected ResponseEntity<?> page(Object object) {
    return super.ok(pagedObject(object));
  }

  private Object pagedObject(Object object) {
    if (object instanceof Page) {
      return pagedResources((Page<?>) object);
    } else if (object instanceof Collection) {
      Collection<?> collection = (Collection<?>) object;
      int number = properties.getPageable().isOneIndexedParameters() ? 1 : 0;
      int size = collection.size();
      return new PagedResources<>(new PageMetadata(number, size, 1, size), collection);
    } else if (object != null && object.getClass().isArray()) {
      Object[] array = (Object[]) object;
      int number = properties.getPageable().isOneIndexedParameters() ? 1 : 0;
      int size = array.length;
      return new PagedResources<>(new PageMetadata(number, size, 1, size), array);
    } else {
      return object;
    }
  }

  private PagedResources<?> pagedResources(Page<?> object) {
    int number =
        properties.getPageable().isOneIndexedParameters() ? object.getNumber() + 1
            : object.getNumber();
    return new PagedResources<>(new PageMetadata(number, object.getSize(), object.getTotalPages(),
        object.getTotalElements()), object.getContent());
  }

}

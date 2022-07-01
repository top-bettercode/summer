package top.bettercode.simpleframework.data.jpa;

import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import top.bettercode.simpleframework.web.BaseController;
import top.bettercode.simpleframework.web.PagedResources;
import top.bettercode.simpleframework.web.PagedResources.PageMetadata;
import top.bettercode.simpleframework.web.RespExtra;

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

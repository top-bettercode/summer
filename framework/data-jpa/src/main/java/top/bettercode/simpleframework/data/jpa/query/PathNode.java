package top.bettercode.simpleframework.data.jpa.query;

import java.util.ArrayList;
import java.util.List;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

class PathNode {

  String name;
  @Nullable
  PathNode parent;
  List<PathNode> siblings = new ArrayList<>();
  @Nullable
  Object value;

  PathNode(String edge, @Nullable PathNode parent, @Nullable Object value) {

    this.name = edge;
    this.parent = parent;
    this.value = value;
  }

  PathNode add(String attribute, @Nullable Object value) {

    PathNode node = new PathNode(attribute, this, value);
    siblings.add(node);
    return node;
  }

  boolean spansCycle() {

    if (value == null) {
      return false;
    }

    String identityHex = ObjectUtils.getIdentityHexString(value);
    PathNode current = parent;

    while (current != null) {

      if (current.value != null && ObjectUtils.getIdentityHexString(current.value)
          .equals(identityHex)) {
        return true;
      }
      current = current.parent;
    }

    return false;
  }

  @Override
  public String toString() {

    StringBuilder sb = new StringBuilder();
    if (parent != null) {
      sb.append(parent);
      sb.append(" -");
      sb.append(name);
      sb.append("-> ");
    }

    sb.append("[{ ");
    sb.append(ObjectUtils.nullSafeToString(value));
    sb.append(" }]");
    return sb.toString();
  }
}

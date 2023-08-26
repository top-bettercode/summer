package top.bettercode.summer.data.jpa.domain;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import javax.annotation.Generated;

/** QPhysicalUser is a Querydsl query type for PhysicalUser */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPhysicalUser extends EntityPathBase<PhysicalUser> {

  private static final long serialVersionUID = 1L;

  public static final QPhysicalUser physicalUser = new QPhysicalUser("physicalUser");

  public final BooleanPath deleted = createBoolean("deleted");

  public final StringPath firstName = createString("firstName");

  public final NumberPath<Integer> id = createNumber("id", Integer.class);

  public final StringPath lastName = createString("lastName");

  public QPhysicalUser(String variable) {
    super(PhysicalUser.class, forVariable(variable));
  }

  public QPhysicalUser(Path<? extends PhysicalUser> path) {
    super(path.getType(), path.getMetadata());
  }

  public QPhysicalUser(PathMetadata metadata) {
    super(PhysicalUser.class, metadata);
  }
}

package top.bettercode.summer.data.jpa.domain;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import javax.annotation.Generated;

/** QBaseUser is a Querydsl query type for BaseUser */
@Generated("com.querydsl.codegen.SupertypeSerializer")
public class QBaseUser extends EntityPathBase<BaseUser> {

  private static final long serialVersionUID = 1L;

  public static final QBaseUser baseUser = new QBaseUser("baseUser");

  public final BooleanPath deleted = createBoolean("deleted");

  public final StringPath firstName = createString("firstName");

  public final NumberPath<Integer> id = createNumber("id", Integer.class);

  public final StringPath lastName = createString("lastName");

  public QBaseUser(String variable) {
    super(BaseUser.class, forVariable(variable));
  }

  public QBaseUser(Path<? extends BaseUser> path) {
    super(path.getType(), path.getMetadata());
  }

  public QBaseUser(PathMetadata metadata) {
    super(BaseUser.class, metadata);
  }
}

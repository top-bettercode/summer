package cn.bestwu.simpleframework.data.test.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QBaseUser is a Querydsl query type for BaseUser
 */
@Generated("com.querydsl.codegen.SupertypeSerializer")
public class QBaseUser extends EntityPathBase<BaseUser> {

    private static final long serialVersionUID = -822413254L;

    public static final QBaseUser baseUser = new QBaseUser("baseUser");

    public final BooleanPath deleted = createBoolean("deleted");

    public final StringPath firstname = createString("firstname");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath lastname = createString("lastname");

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


package top.bettercode.simpleframework.data.test.domain;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import javax.annotation.Generated;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 839697618L;

    public static final QUser user = new QUser("user");

    public final QBaseUser _super = new QBaseUser(this);

    //inherited
    public final BooleanPath deleted = _super.deleted;

    //inherited
    public final StringPath firstname = _super.firstname;

    //inherited
    public final NumberPath<Integer> id = _super.id;

    //inherited
    public final StringPath lastname = _super.lastname;

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}


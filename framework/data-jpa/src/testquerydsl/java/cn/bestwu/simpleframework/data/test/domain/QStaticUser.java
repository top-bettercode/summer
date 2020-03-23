package cn.bestwu.simpleframework.data.test.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QStaticUser is a Querydsl query type for StaticUser
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QStaticUser extends EntityPathBase<StaticUser> {

    private static final long serialVersionUID = 586359255L;

    public static final QStaticUser staticUser = new QStaticUser("staticUser");

    public final QBaseUser _super = new QBaseUser(this);

    //inherited
    public final BooleanPath deleted = _super.deleted;

    //inherited
    public final StringPath firstname = _super.firstname;

    //inherited
    public final NumberPath<Integer> id = _super.id;

    //inherited
    public final StringPath lastname = _super.lastname;

    public QStaticUser(String variable) {
        super(StaticUser.class, forVariable(variable));
    }

    public QStaticUser(Path<? extends StaticUser> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStaticUser(PathMetadata metadata) {
        super(StaticUser.class, metadata);
    }

}


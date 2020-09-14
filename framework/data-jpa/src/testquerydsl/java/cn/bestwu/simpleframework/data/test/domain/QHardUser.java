package cn.bestwu.simpleframework.data.test.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QHardUser is a Querydsl query type for HardUser
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QHardUser extends EntityPathBase<HardUser> {

    private static final long serialVersionUID = 948744413L;

    public static final QHardUser hardUser = new QHardUser("hardUser");

    public final BooleanPath deleted = createBoolean("deleted");

    public final StringPath firstname = createString("firstname");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath lastname = createString("lastname");

    public QHardUser(String variable) {
        super(HardUser.class, forVariable(variable));
    }

    public QHardUser(Path<? extends HardUser> path) {
        super(path.getType(), path.getMetadata());
    }

    public QHardUser(PathMetadata metadata) {
        super(HardUser.class, metadata);
    }

}


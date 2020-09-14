package cn.bestwu.simpleframework.data.jpa.support;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;

public class MySQLUpperCaseStrategy extends SpringPhysicalNamingStrategy {

  @Override
  public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
    String tableName = super.toPhysicalTableName(name,context).getText().toUpperCase();
    return Identifier.toIdentifier(tableName);
  }

}
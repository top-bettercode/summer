package top.bettercode.simpleframework.data.jpa.support.generator;

import java.io.Serializable;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentityGenerator;

/**
 * <p>
 * 高效GUID产生算法(sequence),基于Snowflake实现64位自增ID算法。
 * <p>
 * 优化开源项目 http://git.oschina.net/yu120/sequence
 * </p>
 */
public class SnowflakeIdGenerator extends IdentityGenerator {

  /**
   * 主机和进程的机器码
   */
  private final static Sequence sequence = new Sequence();

  @Override
  public Serializable generate(SharedSessionContractImplementor session, Object object)
      throws MappingException {
    String id = String.valueOf(sequence.nextId());
    if (id != null) {
      return id;
    }
    return super.generate(session, object);
  }
}
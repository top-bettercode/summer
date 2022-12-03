package top.bettercode.summer.tools.sap.connection.pojo;


import java.text.SimpleDateFormat;
import java.util.Date;
import top.bettercode.summer.tools.lang.util.RandomUtil;
import top.bettercode.summer.tools.lang.util.StringUtil;
import top.bettercode.summer.tools.sap.annotation.SapField;

/**
 * sapHead
 */
public class SapHead {

  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
  private static final SimpleDateFormat timeFormate = new SimpleDateFormat("HHmmss");


  /**
   * 接口传输号 20
   */
  @SapField("IFNO")
  private String ifno = RandomUtil.uuid();

  /**
   * 系统Id
   */
  @SapField("SYSID")
  private String sysid;

  /**
   * 接口编号
   */
  @SapField("IFID")
  private String ifid;

  /**
   * 场景编号
   */
  @SapField("SCENEID")
  private String sceneid;

  /**
   * 用户名
   */
  @SapField("SUSER")
  private String suser;

  /**
   * 字符字段，8 个字符长度
   */
  @SapField("SDATE")
  private String sdate = dateFormat.format(new Date());

  /**
   * 长度为6的字符字段
   */
  @SapField("STIME")
  private String stime = timeFormate.format(new Date());

  /**
   * 长度为 40 的字符型字段
   */
  @SapField("SKDATA")
  private String skdata;

  /**
   * 单一字符标识
   */
  @SapField("OPERATION")
  private String operation;

  /**
   * @return 接口传输号
   */
  public String getIfno() {
    return this.ifno;
  }

  /**
   * 设置接口传输号
   *
   * @param ifno 接口传输号
   * @return 接口控制数据
   */
  public SapHead setIfno(String ifno) {
    this.ifno = ifno;
    return this;
  }

  /**
   * @return 系统Id
   */
  public String getSysid() {
    return this.sysid;
  }

  /**
   * 设置系统Id
   *
   * @param sysid 系统Id
   * @return 接口控制数据
   */
  public SapHead setSysid(String sysid) {
    this.sysid = sysid;
    return this;
  }

  /**
   * @return 接口编号
   */
  public String getIfid() {
    return this.ifid;
  }

  /**
   * 设置接口编号
   *
   * @param ifid 接口编号
   * @return 接口控制数据
   */
  public SapHead setIfid(String ifid) {
    this.ifid = ifid;
    return this;
  }

  /**
   * @return 场景编号
   */
  public String getSceneid() {
    return this.sceneid;
  }

  /**
   * 设置场景编号
   *
   * @param sceneid 场景编号
   * @return 接口控制数据
   */
  public SapHead setSceneid(String sceneid) {
    this.sceneid = sceneid;
    return this;
  }

  /**
   * @return 用户名
   */
  public String getSuser() {
    return this.suser;
  }

  /**
   * 设置用户名
   *
   * @param suser 用户名
   * @return 接口控制数据
   */
  public SapHead setSuser(String suser) {
    this.suser = suser;
    return this;
  }

  /**
   * @return 字符字段，8 个字符长度
   */
  public String getSdate() {
    return this.sdate;
  }

  /**
   * 设置字符字段，8 个字符长度
   *
   * @param sdate 字符字段，8 个字符长度
   * @return 接口控制数据
   */
  public SapHead setSdate(String sdate) {
    this.sdate = sdate;
    return this;
  }

  /**
   * @return 长度为6的字符字段
   */
  public String getStime() {
    return this.stime;
  }

  /**
   * 设置长度为6的字符字段
   *
   * @param stime 长度为6的字符字段
   * @return 接口控制数据
   */
  public SapHead setStime(String stime) {
    this.stime = stime;
    return this;
  }

  /**
   * @return 长度为 40 的字符型字段
   */
  public String getSkdata() {
    return this.skdata;
  }

  /**
   * 设置长度为 40 的字符型字段
   *
   * @param skdata 长度为 40 的字符型字段
   * @return 接口控制数据
   */
  public SapHead setSkdata(String skdata) {
    this.skdata = skdata;
    return this;
  }

  /**
   * @return 单一字符标识
   */
  public String getOperation() {
    return this.operation;
  }

  /**
   * 设置单一字符标识
   *
   * @param operation 单一字符标识
   * @return 接口控制数据
   */
  public SapHead setOperation(String operation) {
    this.operation = operation;
    return this;
  }

  @Override
  public String toString() {
    return StringUtil.json(this);
  }
}

package cn.bestwu.simpleframework.web.validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IDCardInfo {

  private static final Logger logger = LoggerFactory.getLogger(IDCardInfo.class);

  private static ResourceBundle areaCodeBundle;

  static {
    try {
      areaCodeBundle = ResourceBundle.getBundle("areaCode");
    } catch (MissingResourceException e) {
      logger.error("加载配置文件出错", e);
    }
  }


  private String idcard;
  // 省份
  private String province;
  // 城市
  private String city;
  // 区县
  private String region;
  // 年份
  private int year;
  // 月份
  private int month;
  // 日期
  private int day;
  // 性别
  private String gender;
  // 出生日期
  private long birthday;

  private boolean legal;


  private String getString(String key, String defaultVal) {
    return areaCodeBundle.containsKey(key) ? areaCodeBundle.getString(key) : defaultVal;
  }

  public IDCardInfo(String idcard) {
    super();
    this.idcard = idcard;
    if (IDCardUtil.validate(idcard)) {
      legal = true;
      if (idcard.length() == 15) {
        idcard = IDCardUtil.convertFrom15bit(idcard);
      }
      // 获取省份
      String provinceId = idcard.substring(0, 2);
      String cityId = idcard.substring(2, 4);
      String regionId = idcard.substring(4, 6);
      this.province = getString(provinceId + "0000", null);
      this.city = getString(provinceId + cityId + "00", null);
      this.region = getString(provinceId + cityId + regionId, null);

      // 获取性别
      String id17 = idcard.substring(16, 17);
      if (Integer.parseInt(id17) % 2 != 0) {
        this.gender = "男";
      } else {
        this.gender = "女";
      }

      // 获取出生日期
      String birthday = idcard.substring(6, 14);
      Date birthdate;
      try {
        birthdate = new SimpleDateFormat("yyyyMMdd").parse(birthday);
        this.birthday = birthdate.getTime();
        GregorianCalendar currentDay = new GregorianCalendar();
        currentDay.setTime(birthdate);
        this.year = currentDay.get(Calendar.YEAR);
        this.month = currentDay.get(Calendar.MONTH) + 1;
        this.day = currentDay.get(Calendar.DAY_OF_MONTH);
      } catch (ParseException e) {
        // skip
      }
    }
  }

  public String getProvince() {
    return province;
  }

  public void setProvince(String province) {
    this.province = province;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public int getMonth() {
    return month;
  }

  public void setMonth(int month) {
    this.month = month;
  }

  public int getDay() {
    return day;
  }

  public void setDay(int day) {
    this.day = day;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public long getBirthday() {
    return birthday;
  }

  public void setBirthday(long birthday) {
    this.birthday = birthday;
  }

  public boolean isLegal() {
    return legal;
  }

  public void setLegal(boolean legal) {
    this.legal = legal;
  }

  public String getIdcard() {
    return idcard;
  }

  public void setIdcard(String idcard) {
    this.idcard = idcard;
  }

  @Override
  public String toString() {
    if (legal) {
      return "出生地：" + province + city + region + ",生日：" + year + "年" + month + "月" + day + "日,性别："
          + gender;
    } else {
      return "非法身份证号码";
    }
  }

}
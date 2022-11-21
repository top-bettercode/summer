package top.bettercode.simpleframework.web.validator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import top.bettercode.lang.property.PropertiesSource;
import top.bettercode.lang.property.Settings;

public class IDCardInfo {

  private static final PropertiesSource areaCodes = Settings.getAreaCode();

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
  private LocalDate birthday;

  private boolean legal;


  private String getString(String key, String defaultVal) {
    return areaCodes.getOrDefault(key, defaultVal);
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
      String provinceId = Objects.requireNonNull(idcard).substring(0, 2);
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
      this.birthday = LocalDate
          .parse(idcard.substring(6, 14), DateTimeFormatter.ofPattern("yyMMdd"));
      this.year = birthday.getYear();
      this.month = birthday.getMonthValue();
      this.day = birthday.getDayOfMonth();
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

  public LocalDate getBirthday() {
    return birthday;
  }

  public IDCardInfo setBirthday(LocalDate birthday) {
    this.birthday = birthday;
    return this;
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

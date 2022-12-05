# 完整签名说明 #

完整签名由参数加密、user-agent 加密、请求时间加密组成。

## 签名算法

签名由参数MD5加密后的前16位，请求useragent MD5加密后的第17－24位，当前请求时间毫秒数(
从1970年1月1日（UTC/GMT的午夜）开始所经过的毫秒数)除以60000后向下取整MD5加密后的第25至32位组合而成.

### 参数加密

* 筛选
  获取所有请求参数，不包括字节类型参数，如文件、字节流，不包括签名参数“sign”。

* 排序
  将筛选的参数按照参数名第一个字符的键值ASCII码递增排序（字母升序排序），如果遇到相同字符则按照第二个字符的键值ASCII码递增排序，以此类推。如果遇到一个参数多个值（如：q=1&q=2）则转换为一个值为英文逗号（,）分隔的参数（如：q=1,2）

* 拼接
  将排序后的参数与其对应值，组合成“参数=参数值”的格式,加上clientSecret值，并且把这些参数用&字符连接起来。待签名字符串为"
  UTF-8"编码。此时生成的字符串为待签名字符串。
  例如下面的请求示例，参数值都是示例，开发者参考格式即可：

```
      REQUEST URL: https://www.xxx.com?page=0&size=25&type=0&showTime=&start=0&limit=25
      假如clientSecret为:alkdfjaso
      组成的待签名字符串为：
      
        limit=25&page=0&size=25&start=0&type=0&alkdfjaso
      
```

> 注意：

> > 没有值的参数无需传递，也无需包含到待签名数据中；

> > 待签名数据应该是原生值而不是URL
> > Encoding之后的值。例如：调用某接口需要对请求参数email进行数字签名，那么待签名数据应该是email=test@msn.com，而不是email=test%40msn.com。

* 使用MD5对待签名字符串进行加密码,取前16位加密后的字符串为最终签名的前16位。

```html
      
      待签名字符串为：limit=25&page=0&size=25&start=0&type=0&alkdfjaso
      
      加密后值：26d595f645505e93

```

### user-agent 加密

* 当前user-agent加上clientSecret值,MD5加密后取第17－24位。

```html
        假如clientSecret为:alkdfjaso
        user-agent：Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.71 Safari/537.1
        待签名字符串为: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.71 Safari/537.1alkdfjaso
        加密后值：b05354cb
```

### 请求时间加密

* 签名有效时间为1分钟，取当前时数(从1970年1月1日（UTC/GMT的午夜）开始所经过的毫秒数)
  除以60000后向下取整，加上clientSecret值,MD5加密后取第25-32位。

```html
      假如clientSecret为:alkdfjaso
      当前时间：1496714706844
      待签名字符串为：24945245alkdfjaso
      加密后值：f98109df

```

### 最终签名

最终签名为：26d595f645505e93b05354cbf98109df

# 接口签名说明 #

签名算法分[简单签名](simple.md)、[完整签名](complete.md)

## 签名传递方式

* 支持request.header、request.querystring、request.form等方式传递签名，签名参数名为："sign"。推荐使用request.header方便传递。

``` 

Accept: application/json;version=1.0.b
Accept-Language: zh-CN,en-US;q=0.5
Accept-Encoding: gzip, deflate
sign: 26d595f645505e93b05354cbf98109df

或

https://www.xxx.com?page=0&size=25&type=0&showTime=&start=0&limit=25&sign=26d595f645505e93b05354cbf98109df
```

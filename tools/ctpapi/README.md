# JAVA-CTPAPI

库文件：https://github.com/nicai0609/JAVA-CTPAPI.git

##  libthostmduserapi_se.so libthosttraderapi_se.so 动态链接路径修改为相对路径

解决 System.load 方法报错：无法打开共享对象文件: 没有那个文件或目录

```bash
patchelf --replace-needed libthostmduserapi_se.so '$ORIGIN/libthostmduserapi_se.so' --replace-needed libthosttraderapi_se.so '$ORIGIN/libthosttraderapi_se.so' libthostapi_wrap.so
```

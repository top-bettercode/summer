# 求解器

## libcopt_cpp.so 动态链接路径修改为相对路径

解决 System.load 方法报错：libcoptjniwrap.so: libcopt_cpp.so: 无法打开共享对象文件: 没有那个文件或目录

```bash
patchelf --replace-needed libcopt_cpp.so '$ORIGIN/libcopt_cpp.so' libcoptjniwrap.so
```

* profile gradle 插件

* 使用方法：

引入：

```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'top.bettercode.gradle:profile-plugin:1.0'
    }
}

apply plugin: 'top.bettercode.profile'
```
或

```groovy
plugins {
  id "top.bettercode.profile" version "1.0"
}
```

使用：

```groovy

//profile
profile {
    closure {
        copy {
            from "config/${profile}"
            into 'src/main/resources'
        }
    }
}

如果定义了 profile.actives 或 profile.defaultActive, profile 定义应在publish task之前
```
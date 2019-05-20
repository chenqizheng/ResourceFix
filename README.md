# ResourceFix

## 前言

该插件可以为所有Activity（包括aar中）重写getResource方法，并且回调给自定义的类处理。比如如下两个需求：

1. 需求1：国际化App，部分页面不做国际化，但是公有字符串像Loading，OK等文案又已经增加了翻译。可以使用本插件将不需要国际化的界面设置成中文。

2. 需求2：App做了字体调整功能，将整个Resource的fontScale变成了1.5，但是部分页面又想不去放大，要将sp改为dp。又太繁琐，可以使用本插件，将fontScale强制为1

## 使用

1. 在project的build.gradle，增加如下：

```groovy
buildscript {
    repositories {
        google()
        jcenter()
        maven{
            url 'https://dl.bintray.com/chenqizheng/maven'
        }

    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.4.0'
        classpath 'me.chen.resourcefix:gradle-plugin:1.0.0'
    }
}
```

2. 在Application的build.gradle增加如下配置：


```groovy
apply plugin: 'me.chen.resourcefix'

resourceFix {
     //自定义实现的工具类名
    insertClass = "me.chen.resourcefix.ResourceFixUtils"
    //自定义实现的insertClass的静态方法
    insertStaticMethod = "resetFontScale"
}
```

## 参考链接

 1. [Router](https://github.com/chenenyu/Router)
 2. [AutoRegister](https://github.com/luckybilly/AutoRegister)
 3. [如何修改Jar File](https://riptutorial.com/java/example/12965/how-to-edit-jar-files-with-asm)




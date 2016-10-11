# AndroidDexDemo
Dynamic Load Class via DexClassLoader and PathClassLoader


## App里面的ClassLoader
- Activity#onCreate时，context.getClassLoader是PathClassLoader
- String.class.getClassLoader是BootClassLoader
- Activity.class.getClassLoader是BootClassLoader
- 说明App中用户定义的类是通过PathClassLoader加载的；


## PathClassLoader
- 仅能加载已安装的Apk，没有DexOpt；
- 这个类型主要是Framework加载安装的App的；
- Framework里面创建PathClassLoader时，parent为ClassLoader.getSystemClassLoader()，即PathClassLoader，然而这个PathClassLoader其parent是BootClassLoader；

## DexClassLoader
- 可用来加载外部Dex/Apk；
- 需要指定一个DexOpt目录，一般要放到App的Private目录里，放到SD卡会被修改的风险；
- 这个加载器就是让开发者使用的；
- 使用dx工具可将Java包（.jar）转化成DexClassLoader可加载的.dex文件；

## DexClassLoader的parent问题
new DexClassLoader()时，指定DexClassLoader的parent是哪个？
一、App的PathClassLoader
二、BootClassLoader
二者都能加载成功，但有所区别，做了个小实验：
第一种情况的加载过程应该是：
DexClassLoader加载，委托给PathClassLoader，再委托给BootClassLoader，均没加载成功，最后由DexClassLoader本身加载；
第二种情况的加载过程是：
DexClassLoader加载，委托给BootClassLoader，没成功，由DexClassLoader加载；
为了验证，在宿主里面创建了一个与插件相同包路径的类型，
第一种情况下，加载的是宿主里面的类，因为委派给PathClassLoader时就找到了；
第二种情况下，加载的是插件里面的类，因为没有经过PathClassLoader；
另外：
第一种情况下，插件里面的代码可以反射到宿主里面的代码；
第二种情况下，插件里面的代码不能反射到宿主里面的代码；

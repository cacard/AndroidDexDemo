package cacard.androiddexdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * Created by cunqingli on 2016/9/7.
 */
public class MainActivity extends Activity {

    final String classFullName = "com.cacard.dex.DynamicClass";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadClassFromDex();
    }

    /**
     * DexClassLoader可以从未安装的.dex/.apk/.jar中加载
     * <p/>
     * 1, 普通.class -> .jar
     * 2，通过dx tool .jar -> .dex(.apk/.jar)
     * 3，Load Class From .dex
     */
    private void loadClassFromDex() {

        // 要加载的apk所在目录
        String dexPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "dc.dex";
        // opt释放位置 4.1之后不能放到sd
        String optDir = getDir("_dc_opt", MODE_PRIVATE).getAbsolutePath();
        checkDir(optDir);
        // 依赖的lib
        String libSearchPath = null;
        // 父Loader
        ClassLoader parentClassLoader = getClassLoader();

        // 看一下这个parentClassLoader是什么？
        log("parentClassLoader:" + parentClassLoader.toString()); // PathClassLoader
        log("parentClassLoader's parent:" + parentClassLoader.getParent().toString()); // BootClassLoader

        // 加入把parentClassLoader改成BootClassLoader呢?
        // 完全是可行的。会有什么缺点/问题吗？
        // 重点来了！
        // 1，如果DexClassLoader的parent是context.getClassLoader（即PathClassLoader），
        //    那么加载过程是：->DexClassLoader->PathClassLoader->BootClassLoader，没找到，最终在DexClassLoader中加载；
        //    假设在宿主里面加个与插件里面同路径名的类，那么会首先在宿主里面加载！
        //    而且，插件里面可以通过Class.forName加载宿主里面的类型（用户自定义类）！
        // 2，如果DexClassLoader指定为BootClassLoader
        //    加载过程是：->DexClassLoader->BootClassLoader，没找到，最终在DexClassLoader中加载；
        //    如果宿主里面有与插件里面同路径名的类，也不会加载宿主里面的类，而是插件的！
        //    而且，插件里面不能通过Class.forName加载宿主里面的类型！
        //parentClassLoader = parentClassLoader.getParent();

        try {
            DexClassLoader dexClassLoader = new DexClassLoader(dexPath, optDir, libSearchPath, parentClassLoader);
            Class<?> clazz = dexClassLoader.loadClass(classFullName);
            String s = clazz.getSimpleName();

            Constructor<?> ctor = clazz.getConstructor(new Class<?>[]{});
            ctor.setAccessible(true);
            Object obj = ctor.newInstance(new Object[]{});

            Method getValue = clazz.getDeclaredMethod("getValue", new Class[]{});
            Integer r = (Integer) getValue.invoke(obj, new Object[]{});
            log("getValue:" + String.valueOf(r));


            // 测试插件是否可以访问宿主的方法
            Method visitHost = clazz.getDeclaredMethod("visitHost", new Class[]{});
            String msg = (String) visitHost.invoke(obj, new Object[]{});
            log("visitHost:" + msg);

        } catch (Exception e) {
            e.printStackTrace();

            //opt放到sd后出错。
            //Optimized data directory /storage/emulated/0/dcopt is not owned by the current user. Shared storage cannot protect your application from code injection attacks.
        }
    }

    /**
     * PathClassLoader只能从已安装的class中加载
     */
    private void loadClassFromInstalledApk() {

    }

    private void log(String msg) {
        Log.i("dexlog", msg);
    }

    private void checkDir(String p) {
        File f = new File(p);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

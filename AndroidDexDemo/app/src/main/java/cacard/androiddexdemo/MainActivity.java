package cacard.androiddexdemo;

import android.app.Activity;
import android.app.usage.UsageEvents;
import android.os.Bundle;
import android.os.Environment;

import java.io.File;

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
        String dexPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "dc.apk";
        // opt释放位置
        String optDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "dcopt";
        // 依赖的lib
        String libSearchPath = null;
        // 父Loader
        ClassLoader parentClassLoader = getClassLoader();

        DexClassLoader dexClassLoader = new DexClassLoader(dexPath, optDir, libSearchPath, parentClassLoader);

        try {
            Class<?> clazz = dexClassLoader.loadClass(classFullName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * PathClassLoader只能从已安装的class中加载
     */
    private void loadClassFromInstalledApk() {

    }
}

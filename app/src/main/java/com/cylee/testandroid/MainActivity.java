package com.cylee.testandroid;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        File apkFile = new File("sdcard/testplugin.apk");
        if (apkFile.exists()) {
            DexClassLoader dexClassLoader = new DexClassLoader(apkFile.getAbsolutePath(),getFilesDir().getAbsolutePath(),
                    null, getClassLoader());
            try {
                Class util = dexClassLoader.loadClass("com.cylee.testplugin.UtilTest");
                util.getDeclaredMethod("test").invoke(null, null);

                Method testMethod = util.getDeclaredMethod("testResource", new Class[]{Context.class});
                testMethod.invoke(null, this);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e){
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}

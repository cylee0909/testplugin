package com.cylee.testandroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cylee.testplugin.UtilTest;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.zip.ZipFile;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            test1();
            test2();
            test3();
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public void test2() throws Exception {
        injectDex();
        Class util = getClassLoader().loadClass("com.cylee.testplugin.UtilTest");
        util.getDeclaredMethod("test").invoke(null, null);
    }

    public void test3() throws Exception {
        injectDex();
        UtilTest.test();
    }


    private void injectDex() throws Exception {
        File apkFile = new File("sdcard/testplugin.apk");

        // inject dex element
        DexFile dexFile = DexFile.loadDex(apkFile.getAbsolutePath(), new File(getFilesDir(), "extra.dex").getAbsolutePath(), 0);
        Class dexElementClass = Class.forName("dalvik.system.DexPathList$Element");
        Constructor firstConstructor = dexElementClass.getConstructors()[0];
        Class[] params = firstConstructor.getParameterTypes();

        Object dexElement = null;
        if (params.length == 3) {
            if (params[1].equals(ZipFile.class)) {
                ZipFile zipFile = new ZipFile(apkFile);
                dexElement = firstConstructor.newInstance(apkFile, zipFile, dexFile);
            } else {
                dexElement = firstConstructor.newInstance(apkFile, apkFile, dexFile);
            }
        } else if (params.length == 4) {
            dexElement =  firstConstructor.newInstance(apkFile, false, apkFile, dexFile);
        }

        Field pathListField = getDeclaredField(DexClassLoader.class.getSuperclass(), "pathList");
        Object pathList = pathListField.get(getClassLoader());
        expandFieldArray(pathList, "dexElements", new Object[]{dexElement});
    }

    private static void expandFieldArray(Object instance, String fieldName,
                                         Object[] extraElements) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        Field jlrField = getDeclaredField(instance.getClass(), fieldName);
        Object[] original = (Object[]) jlrField.get(instance);
        Object[] combined = (Object[]) Array.newInstance(
                original.getClass().getComponentType(), original.length + extraElements.length);
        System.arraycopy(original, 0, combined, 0, original.length);
        System.arraycopy(extraElements, 0, combined, original.length, extraElements.length);
        jlrField.set(instance, combined);
    }

    private static Field getDeclaredField(Class cls, String fieldName) {
        try {
            Field field = cls.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    /**
     * 简单调用另一个apk中方法
     */
    public void test1() {
        File apkFile = new File("sdcard/testplugin.apk");
        if (apkFile.exists()) {
            DexClassLoader dexClassLoader = new DexClassLoader(apkFile.getAbsolutePath(),getFilesDir().getAbsolutePath(),
                    null, getClassLoader());
            try {
                Class util = dexClassLoader.loadClass("com.cylee.testplugin.UtilTest");
                util.getDeclaredMethod("test").invoke(null, null);

//                Method testMethod = util.getDeclaredMethod("testResource", new Class[]{Context.class});
//                testMethod.invoke(null, this);
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

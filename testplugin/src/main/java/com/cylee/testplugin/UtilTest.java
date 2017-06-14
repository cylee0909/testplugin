package com.cylee.testplugin;

import android.content.Context;

/**
 * Created by cylee on 2017/6/14.
 */

public class UtilTest {
    public static void test() {
        System.out.println("Hello from test");
    }

    public static void testResource(Context context) {
        System.out.println(context.getResources().getString(R.string.hello_text));
    }
}

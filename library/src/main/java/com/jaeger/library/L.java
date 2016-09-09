package com.jaeger.library;

import android.util.Log;

/**
 * Created by Jaeger on 16/8/31.
 *
 * Email: chjie.jaeger@gamil.com
 * GitHub: https://github.com/laobie
 */
public class L {
    private final static String TAG_DEBUG = "DEBUG";

    public final static boolean IS_DEBUG = true;

    public static void d(String msg) {
        d(TAG_DEBUG, msg);
    }

    public static void d(String tag, String msg) {
        if (IS_DEBUG) {
            Log.d(tag, msg);
        }
    }
}

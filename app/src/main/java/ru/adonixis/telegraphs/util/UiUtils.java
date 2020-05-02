package ru.adonixis.telegraphs.util;

import android.content.res.Resources;

public class UiUtils {

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int spToPx(float sp) {
        return (int) (sp * Resources.getSystem().getDisplayMetrics().scaledDensity);
    }
}

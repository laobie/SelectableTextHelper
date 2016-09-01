package com.jaeger.testtextview;

import android.text.Layout;
import android.widget.TextView;

/**
 * Created by Jaeger on 16/8/31.
 *
 * Email: chjie.jaeger@gamil.com
 * GitHub: https://github.com/laobie
 */
public class SelectUtil {
    public static int getPreciseOffset(TextView textView, int x, int y) {
        Layout layout = textView.getLayout();
        if (layout != null) {
            int topVisibleLine = layout.getLineForVertical(y);
            int offset = layout.getOffsetForHorizontal(topVisibleLine, x);

            int offsetX = (int) layout.getPrimaryHorizontal(offset);
            if (offsetX > x) {
                return layout.getOffsetToLeftOf(offset);
            } else {
                return offset;
            }
        } else {
            return -1;
        }
    }
}

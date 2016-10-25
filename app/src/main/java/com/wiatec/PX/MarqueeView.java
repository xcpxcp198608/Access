package com.wiatec.PX;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by PX on 2016/9/4.
 */
public class MarqueeView extends TextView {
    public MarqueeView(Context context) {
        super(context);
    }

    public MarqueeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}

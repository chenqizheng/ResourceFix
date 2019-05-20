package me.chen.sample;

import android.content.res.Configuration;
import android.content.res.Resources;

public class ResourceFixUtils {
    public static Resources resetFontScale(Resources resources) {
        Resources res = resources;
        Configuration conf = res.getConfiguration();
        conf.fontScale = 1.0f;
        res.updateConfiguration(conf, res.getDisplayMetrics());
        return res;
    }
}

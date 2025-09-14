package com.walhalla.utils;

import android.content.Context;
import android.content.res.AssetManager;

import com.walhalla.ui.DLog;

import java.io.IOException;
import java.io.InputStream;

public class AssetUtils {
    public static String loadFromAsset(Context context, String fileName) {
        AssetManager assetManager = context.getAssets();
        try {
            InputStream inputStream = assetManager.open(fileName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            return new String(buffer);
        } catch (IOException e) {
            DLog.handleException(e);
            return "";
        }
    }
}

package com.example.translator.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.translator.data.model.UserPreferences;

public class ThemeManager {

    public static void applyTheme(String theme) {
        switch (theme) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static boolean isDarkTheme(Context context) {
        int nightModeFlags = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void applyFontSize(Activity activity, String fontSize) {
        // Có thể implement font size scaling nếu cần
        // Hiện tại chỉ log để biết font size được set
        android.util.Log.d("ThemeManager", "Font size set to: " + fontSize);
    }

    public static void applyUserPreferences(Context context, UserPreferences preferences) {
        if (preferences != null) {
            applyTheme(preferences.getTheme());
            if (context instanceof Activity) {
                applyFontSize((Activity) context, preferences.getFontSize());
            }
        }
    }
}
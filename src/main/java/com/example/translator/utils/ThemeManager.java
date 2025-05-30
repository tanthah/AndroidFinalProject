package com.example.translator.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.translator.data.model.UserPreferences;

public class ThemeManager {

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME = "current_theme";

    public static void applyTheme(String theme) {
        android.util.Log.d("ThemeManager", "Applying theme: " + theme);
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
        android.util.Log.d("ThemeManager", "Theme applied: " + theme);
    }

    // Lưu theme vào SharedPreferences
    public static void saveTheme(Context context, String theme) {
        android.util.Log.d("ThemeManager", "Saving theme: " + theme);
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_THEME, theme).commit(); // Dùng commit() thay vì apply() để đảm bảo lưu ngay lập tức
        applyTheme(theme);
        android.util.Log.d("ThemeManager", "Theme saved and applied: " + theme);
    }

    // Lấy theme từ SharedPreferences
    public static String getSavedTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_THEME, "light");
    }

    // Áp dụng theme đã lưu
    public static void applySavedTheme(Context context) {
        String savedTheme = getSavedTheme(context);
        android.util.Log.d("ThemeManager", "Applying saved theme: " + savedTheme);
        applyTheme(savedTheme);
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
            saveTheme(context, preferences.getTheme());
            if (context instanceof Activity) {
                applyFontSize((Activity) context, preferences.getFontSize());
            }
        }
    }
}
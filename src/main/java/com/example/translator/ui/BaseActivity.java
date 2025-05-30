package com.example.translator.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.translator.utils.ThemeManager;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Áp dụng theme trước khi gọi super.onCreate()
        ThemeManager.applySavedTheme(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Đảm bảo theme được áp dụng khi activity resume
        ThemeManager.applySavedTheme(this);
    }
}
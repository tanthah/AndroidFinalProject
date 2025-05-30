package com.example.translator.ui.settings;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.translator.R;
import com.example.translator.TranslatorApplication;
import com.example.translator.data.model.UserPreferences;
import com.example.translator.ui.text.LanguageSpinnerAdapter;
import com.example.translator.ui.text.LanguageSpinnerAdapter.LanguageSpinnerItem;
import com.example.translator.utils.ThemeManager;
import com.google.android.material.slider.Slider;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    private SettingsViewModel viewModel;
    private ExecutorService executor;

    private Spinner spinnerDefaultSourceLanguage;
    private Spinner spinnerDefaultTargetLanguage;
    private Spinner spinnerTheme;
    private Spinner spinnerFontSize;
    private Switch switchAutoDetectLanguage;
    private Switch switchTtsEnabled;
    private Switch switchCameraAutoTranslate;
    private Button btnSave;

    // Thêm speech settings
    private Slider sliderSpeechRate;
    private TextView tvSpeechRateLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Áp dụng theme trước khi tạo activity
        ThemeManager.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        executor = Executors.newSingleThreadExecutor();
        setupActionBar();
        initializeViews();
        setupViewModel();
        setupClickListeners();
        observeViewModel();
    }

    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }
    }

    private void initializeViews() {
        spinnerDefaultSourceLanguage = findViewById(R.id.spinner_default_source_language);
        spinnerDefaultTargetLanguage = findViewById(R.id.spinner_default_target_language);
        spinnerTheme = findViewById(R.id.spinner_theme);
        spinnerFontSize = findViewById(R.id.spinner_font_size);
        switchAutoDetectLanguage = findViewById(R.id.switch_auto_detect_language);
        switchTtsEnabled = findViewById(R.id.switch_tts_enabled);
        switchCameraAutoTranslate = findViewById(R.id.switch_camera_auto_translate);
        btnSave = findViewById(R.id.btn_save);

        // Thêm speech settings views
        sliderSpeechRate = findViewById(R.id.slider_speech_rate);
        tvSpeechRateLabel = findViewById(R.id.tv_speech_rate_label);

        // Setup speech rate slider
        if (sliderSpeechRate != null) {
            sliderSpeechRate.setValueFrom(0.5f);
            sliderSpeechRate.setValueTo(1.5f);
            sliderSpeechRate.setStepSize(0.25f);
            sliderSpeechRate.setValue(1.0f);

            sliderSpeechRate.addOnChangeListener((slider, value, fromUser) -> {
                updateSpeechRateLabel(value);
            });

            updateSpeechRateLabel(1.0f);
        }
    }

    private void updateSpeechRateLabel(float rate) {
        if (tvSpeechRateLabel != null) {
            String rateText = getSpeechRateText(rate);
            tvSpeechRateLabel.setText("Speech Speed: " + rateText);
        }
    }

    private String getSpeechRateText(float rate) {
        if (rate <= 0.5f) return "Very Slow";
        else if (rate <= 0.75f) return "Slow";
        else if (rate <= 1.0f) return "Normal";
        else if (rate <= 1.25f) return "Fast";
        else return "Very Fast";
    }

    private void setupViewModel() {
        TranslatorApplication application = (TranslatorApplication) getApplication();
        SettingsViewModel.SettingsViewModelFactory factory = new SettingsViewModel.SettingsViewModelFactory(
                application.getUserRepository(),
                application.getLanguageRepository()
        );
        viewModel = new ViewModelProvider(this, factory).get(SettingsViewModel.class);
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> saveSettings());

        // Theme change listener để preview theme
        spinnerTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                // Preview theme change (optional)
                String theme;
                switch (position) {
                    case 0: theme = "light"; break;
                    case 1: theme = "dark"; break;
                    case 2: theme = "system"; break;
                    default: theme = "light";
                }
                // Uncomment below line if you want immediate theme preview
                // ThemeManager.saveTheme(SettingsActivity.this, theme);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void observeViewModel() {
        viewModel.supportedLanguages.observe(this, this::setupLanguageSpinners);

        viewModel.userPreferences.observe(this, preferences -> {
            if (preferences != null) {
                loadSettings(preferences);
            }
        });
    }

    private void setupLanguageSpinners(List<com.example.translator.data.model.Language> languages) {
        if (languages == null || languages.isEmpty()) return;

        LanguageSpinnerAdapter adapter = new LanguageSpinnerAdapter(this, languages);

        spinnerDefaultSourceLanguage.setAdapter(adapter);
        spinnerDefaultTargetLanguage.setAdapter(adapter);

        // Setup theme spinner
        String[] themeOptions = {"Light", "Dark", "System"};
        ArrayAdapter<String> themeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, themeOptions);
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTheme.setAdapter(themeAdapter);

        // Setup font size spinner
        String[] fontSizeOptions = {"Small", "Medium", "Large"};
        ArrayAdapter<String> fontSizeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fontSizeOptions);
        fontSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFontSize.setAdapter(fontSizeAdapter);
    }

    private void loadSettings(UserPreferences preferences) {
        // Set language selections
        if (spinnerDefaultSourceLanguage.getAdapter() instanceof LanguageSpinnerAdapter) {
            LanguageSpinnerAdapter sourceAdapter = (LanguageSpinnerAdapter) spinnerDefaultSourceLanguage.getAdapter();
            for (int i = 0; i < sourceAdapter.getCount(); i++) {
                LanguageSpinnerItem item = sourceAdapter.getItem(i);
                if (item.language.getLanguageCode().equals(preferences.getDefaultSourceLanguage())) {
                    spinnerDefaultSourceLanguage.setSelection(i);
                    break;
                }
            }
        }

        if (spinnerDefaultTargetLanguage.getAdapter() instanceof LanguageSpinnerAdapter) {
            LanguageSpinnerAdapter targetAdapter = (LanguageSpinnerAdapter) spinnerDefaultTargetLanguage.getAdapter();
            for (int i = 0; i < targetAdapter.getCount(); i++) {
                LanguageSpinnerItem item = targetAdapter.getItem(i);
                if (item.language.getLanguageCode().equals(preferences.getDefaultTargetLanguage())) {
                    spinnerDefaultTargetLanguage.setSelection(i);
                    break;
                }
            }
        }

        // Set theme selection
        int themeIndex;
        switch (preferences.getTheme()) {
            case "light": themeIndex = 0; break;
            case "dark": themeIndex = 1; break;
            case "system": themeIndex = 2; break;
            default: themeIndex = 0;
        }
        spinnerTheme.setSelection(themeIndex);

        // Set font size selection
        int fontSizeIndex;
        switch (preferences.getFontSize()) {
            case "small": fontSizeIndex = 0; break;
            case "medium": fontSizeIndex = 1; break;
            case "large": fontSizeIndex = 2; break;
            default: fontSizeIndex = 1;
        }
        spinnerFontSize.setSelection(fontSizeIndex);

        // Set switches
        switchAutoDetectLanguage.setChecked(preferences.isAutoDetectLanguage());
        switchTtsEnabled.setChecked(preferences.isTtsEnabled());
        switchCameraAutoTranslate.setChecked(preferences.isCameraAutoTranslate());

        // Set speech rate
        if (sliderSpeechRate != null) {
            sliderSpeechRate.setValue(preferences.getSpeechRate());
            updateSpeechRateLabel(preferences.getSpeechRate());
        }
    }

    private void saveSettings() {
        try {
            LanguageSpinnerItem sourceItem = (LanguageSpinnerItem) spinnerDefaultSourceLanguage.getSelectedItem();
            LanguageSpinnerItem targetItem = (LanguageSpinnerItem) spinnerDefaultTargetLanguage.getSelectedItem();

            String sourceLanguage = sourceItem != null ? sourceItem.language.getLanguageCode() : "en";
            String targetLanguage = targetItem != null ? targetItem.language.getLanguageCode() : "vi";

            String theme;
            switch (spinnerTheme.getSelectedItemPosition()) {
                case 0: theme = "light"; break;
                case 1: theme = "dark"; break;
                case 2: theme = "system"; break;
                default: theme = "light";
            }

            String fontSize;
            switch (spinnerFontSize.getSelectedItemPosition()) {
                case 0: fontSize = "small"; break;
                case 1: fontSize = "medium"; break;
                case 2: fontSize = "large"; break;
                default: fontSize = "medium";
            }

            float speechRate = sliderSpeechRate != null ? sliderSpeechRate.getValue() : 1.0f;

            UserPreferences preferences = new UserPreferences(
                    sourceLanguage,
                    targetLanguage,
                    theme,
                    switchAutoDetectLanguage.isChecked(),
                    switchTtsEnabled.isChecked(),
                    switchCameraAutoTranslate.isChecked(),
                    fontSize,
                    speechRate
            );

            // Lưu theme ngay lập tức vào SharedPreferences
            String currentTheme = ThemeManager.getSavedTheme(this);
            boolean themeChanged = !theme.equals(currentTheme);

            if (themeChanged) {
                android.util.Log.d("SettingsActivity", "Theme changed from " + currentTheme + " to " + theme);
                ThemeManager.saveTheme(this, theme);
            }

            // Lưu các settings khác vào database
            executor.execute(() -> {
                try {
                    viewModel.updateUserPreferences(preferences);
                    android.util.Log.d("SettingsActivity", "User preferences saved to database");

                    runOnUiThread(() -> {
                        Toast.makeText(SettingsActivity.this, "Settings saved successfully", Toast.LENGTH_SHORT).show();

                        // Chỉ recreate nếu theme thay đổi
                        if (themeChanged) {
                            android.util.Log.d("SettingsActivity", "Recreating activity due to theme change");
                            // Delay một chút để đảm bảo theme đã được áp dụng
                            new android.os.Handler().postDelayed(() -> {
                                recreate();
                            }, 100);
                        } else {
                            finish();
                        }
                    });
                } catch (Exception e) {
                    android.util.Log.e("SettingsActivity", "Error saving to database", e);
                    runOnUiThread(() -> {
                        Toast.makeText(SettingsActivity.this, "Error saving settings", Toast.LENGTH_SHORT).show();
                    });
                }
            });

        } catch (Exception e) {
            android.util.Log.e("SettingsActivity", "Error in saveSettings", e);
            Toast.makeText(this, "Error saving settings", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
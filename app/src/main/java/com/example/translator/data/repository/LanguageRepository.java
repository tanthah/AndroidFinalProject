package com.example.translator.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import com.example.translator.data.local.AppDatabase;
import com.example.translator.data.local.dao.LanguageDao;
import com.example.translator.data.model.Language;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LanguageRepository {
    private static final String TAG = "LanguageRepository";
    private LanguageDao languageDao;
    private ExecutorService executor;

    public LanguageRepository(Context context) {
        AppDatabase database = AppDatabase.getDatabase(context);
        languageDao = database.languageDao();
        executor = Executors.newFixedThreadPool(4);
    }

    public LiveData<List<Language>> getAllSupportedLanguages() {
        return languageDao.getAllSupportedLanguages();
    }

    public void getLanguageByCode(String code, LanguageCallback callback) {
        if (callback == null) return;

        executor.execute(() -> {
            try {
                Language language = languageDao.getLanguageByCode(code);
                callback.onResult(language);
            } catch (Exception e) {
                Log.e(TAG, "Error getting language by code: " + code, e);
                callback.onResult(null);
            }
        });
    }

    public void initializeSupportedLanguages() {
        executor.execute(() -> {
            try {
                Log.d(TAG, "Initializing supported languages...");

                // Clear existing languages first
                languageDao.clearAll();

                List<Language> supportedLanguages = Arrays.asList(
                        // Major languages with full support (camera + text translation)
                        new Language("en", "English", "English", true, true, true, true),
                        new Language("vi", "Vietnamese", "Tiếng Việt", true, true, false, true),
                        new Language("es", "Spanish", "Español", true, true, false, true),
                        new Language("fr", "French", "Français", true, true, false, true),
                        new Language("de", "German", "Deutsch", true, true, false, true),
                        new Language("it", "Italian", "Italiano", true, true, false, true),
                        new Language("pt", "Portuguese", "Português", true, true, false, true),
                        new Language("ru", "Russian", "Русский", true, true, false, true),

                        // Asian languages với camera support
                        new Language("zh", "Chinese", "中文", true, true, false, true),
                        new Language("ja", "Japanese", "日本語", true, true, false, true),
                        new Language("ko", "Korean", "한국어", true, true, false, true),
                        new Language("th", "Thai", "ไทย", true, true, false, true),
                        new Language("hi", "Hindi", "हिन्दी", true, true, false, true),

                        // European languages
                        new Language("nl", "Dutch", "Nederlands", true, true, false, false),
                        new Language("sv", "Swedish", "Svenska", true, true, false, false),
                        new Language("da", "Danish", "Dansk", true, true, false, false),
                        new Language("no", "Norwegian", "Norsk", true, true, false, false),
                        new Language("fi", "Finnish", "Suomi", true, true, false, false),
                        new Language("pl", "Polish", "Polski", true, true, false, false),
                        new Language("cs", "Czech", "Čeština", true, true, false, false),
                        new Language("hu", "Hungarian", "Magyar", true, true, false, false),

                        // Other important languages
                        new Language("ar", "Arabic", "العربية", true, true, false, true),
                        new Language("tr", "Turkish", "Türkçe", true, true, false, false),
                        new Language("af", "Afrikaans", "Afrikaans", true, true, false, false),

                        // Additional Asian languages
                        new Language("id", "Indonesian", "Bahasa Indonesia", true, true, false, false),
                        new Language("ms", "Malay", "Bahasa Melayu", true, true, false, false),
                        new Language("tl", "Filipino", "Filipino", true, true, false, false),
                        new Language("my", "Burmese", "မြန်မာ", true, true, false, false),
                        new Language("km", "Khmer", "ខ្មែរ", true, true, false, false),
                        new Language("lo", "Lao", "ລາວ", true, true, false, false),

                        // Additional European languages
                        new Language("ro", "Romanian", "Română", true, true, false, false),
                        new Language("bg", "Bulgarian", "Български", true, true, false, false),
                        new Language("hr", "Croatian", "Hrvatski", true, true, false, false),
                        new Language("sr", "Serbian", "Српски", true, true, false, false),
                        new Language("sk", "Slovak", "Slovenčina", true, true, false, false),
                        new Language("sl", "Slovenian", "Slovenščina", true, true, false, false),
                        new Language("et", "Estonian", "Eesti", true, true, false, false),
                        new Language("lv", "Latvian", "Latviešu", true, true, false, false),
                        new Language("lt", "Lithuanian", "Lietuvių", true, true, false, false),
                        new Language("mt", "Maltese", "Malti", true, true, false, false),

                        // Additional languages from other regions
                        new Language("he", "Hebrew", "עברית", true, true, false, false),
                        new Language("fa", "Persian", "فارسی", true, true, false, false),
                        new Language("ur", "Urdu", "اردو", true, true, false, false),
                        new Language("bn", "Bengali", "বাংলা", true, true, false, false),
                        new Language("ta", "Tamil", "தமிழ்", true, true, false, false),
                        new Language("te", "Telugu", "తెలుగు", true, true, false, false),
                        new Language("ml", "Malayalam", "മലയാളം", true, true, false, false),
                        new Language("kn", "Kannada", "ಕನ್ನಡ", true, true, false, false),
                        new Language("gu", "Gujarati", "ગુજરાતી", true, true, false, false),
                        new Language("pa", "Punjabi", "ਪੰਜਾਬੀ", true, true, false, false),

                        // African languages
                        new Language("sw", "Swahili", "Kiswahili", true, true, false, false),
                        new Language("zu", "Zulu", "IsiZulu", true, true, false, false),
                        new Language("xh", "Xhosa", "IsiXhosa", true, true, false, false),

                        // Latin American variants
                        new Language("ca", "Catalan", "Català", true, true, false, false),
                        new Language("eu", "Basque", "Euskera", true, true, false, false),
                        new Language("gl", "Galician", "Galego", true, true, false, false)
                );

                // Insert all languages
                languageDao.insertLanguages(supportedLanguages);
                Log.d(TAG, "Successfully initialized " + supportedLanguages.size() + " languages");

            } catch (Exception e) {
                Log.e(TAG, "Error initializing languages", e);
            }
        });
    }

    public interface LanguageCallback {
        void onResult(Language language);
    }
}
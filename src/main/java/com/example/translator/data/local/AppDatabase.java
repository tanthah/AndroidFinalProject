package com.example.translator.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.example.translator.data.local.dao.LanguageDao;
import com.example.translator.data.local.dao.UserPreferencesDao;
import com.example.translator.data.model.Language;
import com.example.translator.data.model.UserPreferences;

@Database(
        entities = {Language.class, UserPreferences.class},
        version = 3, // Tăng từ 2 lên 3 để thêm speechRate column
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract LanguageDao languageDao();
    public abstract UserPreferencesDao userPreferencesDao();

    private static volatile AppDatabase INSTANCE;

    // Migration từ version 2 lên 3
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Thêm column speechRate với giá trị mặc định là 1.0
            database.execSQL("ALTER TABLE user_preferences ADD COLUMN speechRate REAL NOT NULL DEFAULT 1.0");
        }
    };

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "translator_database"
                            )
                            .addMigrations(MIGRATION_2_3) // Thêm migration
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
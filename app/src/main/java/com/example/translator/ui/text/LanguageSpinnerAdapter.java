package com.example.translator.ui.text;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.translator.R;
import com.example.translator.data.model.Language;
import java.util.ArrayList;
import java.util.List;

public class LanguageSpinnerAdapter extends BaseAdapter {

    private Context context;
    private List<LanguageSpinnerItem> items;
    private LayoutInflater inflater;
    private boolean includeAutoDetect;

    public LanguageSpinnerAdapter(Context context, List<Language> languages) {
        this(context, languages, false);
    }

    public LanguageSpinnerAdapter(Context context, List<Language> languages, boolean includeAutoDetect) {
        this.context = context;
        this.includeAutoDetect = includeAutoDetect;
        this.inflater = LayoutInflater.from(context);
        this.items = new ArrayList<>();

        // Add auto-detect option if requested
        if (includeAutoDetect) {
            this.items.add(new LanguageSpinnerItem("auto", "Auto-detect", "Auto"));
        }

        // Add regular languages
        for (Language language : languages) {
            this.items.add(new LanguageSpinnerItem(language));
        }
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public LanguageSpinnerItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_language_spinner, parent, false);
        }

        LanguageSpinnerItem item = items.get(position);

        TextView tvLanguageName = view.findViewById(R.id.tv_language_name);
        TextView tvNativeName = view.findViewById(R.id.tv_native_name);

        tvLanguageName.setText(item.displayName);
        tvNativeName.setText(item.nativeName);

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    /**
     * Find position of language by code
     */
    public int findPositionByCode(String languageCode) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).languageCode.equals(languageCode)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get language code at position
     */
    public String getLanguageCodeAt(int position) {
        if (position >= 0 && position < items.size()) {
            return items.get(position).languageCode;
        }
        return null;
    }

    public static class LanguageSpinnerItem {
        public final String languageCode;
        public final String displayName;
        public final String nativeName;
        public final Language language; // null for auto-detect

        // Constructor for regular language
        public LanguageSpinnerItem(Language language) {
            this.language = language;
            this.languageCode = language.getLanguageCode();
            this.displayName = language.getLanguageName();
            this.nativeName = language.getNativeName();
        }

        // Constructor for auto-detect or special items
        public LanguageSpinnerItem(String languageCode, String displayName, String nativeName) {
            this.language = null;
            this.languageCode = languageCode;
            this.displayName = displayName;
            this.nativeName = nativeName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    /**
     * Create adapter for source language selection (with auto-detect)
     */
    public static LanguageSpinnerAdapter createSourceAdapter(Context context, List<Language> languages) {
        return new LanguageSpinnerAdapter(context, languages, true);
    }

    /**
     * Create adapter for target language selection (without auto-detect)
     */
    public static LanguageSpinnerAdapter createTargetAdapter(Context context, List<Language> languages) {
        return new LanguageSpinnerAdapter(context, languages, false);
    }
}
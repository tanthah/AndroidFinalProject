package com.example.translator.services;

import android.graphics.Bitmap;
import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions;
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions;
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions;
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions;

public class TextRecognitionService {

    private static final String TAG = "TextRecognitionService";
    private static final int MIN_TEXT_LENGTH = 1;
    private static final int MAX_TEXT_LENGTH = 5000;

    private TextRecognizer latinTextRecognizer;
    private TextRecognizer chineseTextRecognizer;
    private TextRecognizer devanagariTextRecognizer;
    private TextRecognizer japaneseTextRecognizer;
    private TextRecognizer koreanTextRecognizer;

    public TextRecognitionService() {
        try {
            // Initialize different text recognizers for different scripts
            latinTextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            chineseTextRecognizer = TextRecognition.getClient(new ChineseTextRecognizerOptions.Builder().build());
            devanagariTextRecognizer = TextRecognition.getClient(new DevanagariTextRecognizerOptions.Builder().build());
            japaneseTextRecognizer = TextRecognition.getClient(new JapaneseTextRecognizerOptions.Builder().build());
            koreanTextRecognizer = TextRecognition.getClient(new KoreanTextRecognizerOptions.Builder().build());

            Log.d(TAG, "TextRecognitionService initialized successfully with multiple recognizers");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing TextRecognitionService", e);
            // Fallback to Latin only
            latinTextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        }
    }

    public interface TextRecognitionCallback {
        void onSuccess(String recognizedText);
        void onFailure(Exception exception);
    }

    public void recognizeTextFromBitmap(Bitmap bitmap, TextRecognitionCallback callback) {
        if (callback == null) {
            Log.e(TAG, "Callback is null");
            return;
        }

        if (!isValidBitmap(bitmap)) {
            Log.w(TAG, "Invalid bitmap provided");
            callback.onFailure(new IllegalArgumentException("Invalid bitmap"));
            return;
        }

        try {
            Log.d(TAG, "Creating InputImage from bitmap: " + bitmap.getWidth() + "x" + bitmap.getHeight());

            // Enhance bitmap quality for better OCR
            Bitmap enhancedBitmap = enhanceBitmapForOCR(bitmap);
            InputImage image = InputImage.fromBitmap(enhancedBitmap, 0);

            recognizeTextFromImageWithMultipleRecognizers(image, callback);
        } catch (Exception e) {
            Log.e(TAG, "Error creating InputImage from bitmap", e);
            callback.onFailure(e);
        }
    }

    public void recognizeTextFromImage(InputImage inputImage, TextRecognitionCallback callback) {
        if (callback == null) {
            Log.e(TAG, "Callback is null");
            return;
        }

        if (inputImage == null) {
            Log.e(TAG, "InputImage is null");
            callback.onFailure(new IllegalArgumentException("InputImage is null"));
            return;
        }

        recognizeTextFromImageWithMultipleRecognizers(inputImage, callback);
    }

    /**
     * Try multiple recognizers to get the best result
     */
    private void recognizeTextFromImageWithMultipleRecognizers(InputImage inputImage, TextRecognitionCallback callback) {
        Log.d(TAG, "Starting text recognition with multiple recognizers...");

        // Start with Latin recognizer (most common)
        recognizeWithSpecificRecognizer(inputImage, latinTextRecognizer, "Latin", new TextRecognitionCallback() {
            @Override
            public void onSuccess(String recognizedText) {
                if (isGoodRecognitionResult(recognizedText)) {
                    Log.d(TAG, "Latin recognizer succeeded");
                    callback.onSuccess(cleanupRecognizedText(recognizedText));
                } else {
                    // Try other recognizers if Latin didn't work well
                    tryAlternativeRecognizers(inputImage, callback);
                }
            }

            @Override
            public void onFailure(Exception exception) {
                Log.w(TAG, "Latin recognizer failed, trying alternatives");
                tryAlternativeRecognizers(inputImage, callback);
            }
        });
    }

    /**
     * Try alternative recognizers for different scripts
     */
    private void tryAlternativeRecognizers(InputImage inputImage, TextRecognitionCallback callback) {
        // Try Chinese recognizer
        if (chineseTextRecognizer != null) {
            recognizeWithSpecificRecognizer(inputImage, chineseTextRecognizer, "Chinese", new TextRecognitionCallback() {
                @Override
                public void onSuccess(String recognizedText) {
                    if (isGoodRecognitionResult(recognizedText)) {
                        Log.d(TAG, "Chinese recognizer succeeded");
                        callback.onSuccess(cleanupRecognizedText(recognizedText));
                    } else {
                        tryJapaneseRecognizer(inputImage, callback);
                    }
                }

                @Override
                public void onFailure(Exception exception) {
                    tryJapaneseRecognizer(inputImage, callback);
                }
            });
        } else {
            tryJapaneseRecognizer(inputImage, callback);
        }
    }

    private void tryJapaneseRecognizer(InputImage inputImage, TextRecognitionCallback callback) {
        if (japaneseTextRecognizer != null) {
            recognizeWithSpecificRecognizer(inputImage, japaneseTextRecognizer, "Japanese", new TextRecognitionCallback() {
                @Override
                public void onSuccess(String recognizedText) {
                    if (isGoodRecognitionResult(recognizedText)) {
                        Log.d(TAG, "Japanese recognizer succeeded");
                        callback.onSuccess(cleanupRecognizedText(recognizedText));
                    } else {
                        tryKoreanRecognizer(inputImage, callback);
                    }
                }

                @Override
                public void onFailure(Exception exception) {
                    tryKoreanRecognizer(inputImage, callback);
                }
            });
        } else {
            tryKoreanRecognizer(inputImage, callback);
        }
    }

    private void tryKoreanRecognizer(InputImage inputImage, TextRecognitionCallback callback) {
        if (koreanTextRecognizer != null) {
            recognizeWithSpecificRecognizer(inputImage, koreanTextRecognizer, "Korean", new TextRecognitionCallback() {
                @Override
                public void onSuccess(String recognizedText) {
                    if (isGoodRecognitionResult(recognizedText)) {
                        Log.d(TAG, "Korean recognizer succeeded");
                        callback.onSuccess(cleanupRecognizedText(recognizedText));
                    } else {
                        tryDevanagariRecognizer(inputImage, callback);
                    }
                }

                @Override
                public void onFailure(Exception exception) {
                    tryDevanagariRecognizer(inputImage, callback);
                }
            });
        } else {
            tryDevanagariRecognizer(inputImage, callback);
        }
    }

    private void tryDevanagariRecognizer(InputImage inputImage, TextRecognitionCallback callback) {
        if (devanagariTextRecognizer != null) {
            recognizeWithSpecificRecognizer(inputImage, devanagariTextRecognizer, "Devanagari", new TextRecognitionCallback() {
                @Override
                public void onSuccess(String recognizedText) {
                    if (isGoodRecognitionResult(recognizedText)) {
                        Log.d(TAG, "Devanagari recognizer succeeded");
                        callback.onSuccess(cleanupRecognizedText(recognizedText));
                    } else {
                        // All recognizers tried, return best result or error
                        if (recognizedText != null && !recognizedText.trim().isEmpty()) {
                            callback.onSuccess(cleanupRecognizedText(recognizedText));
                        } else {
                            callback.onFailure(new RuntimeException("No text detected with any recognizer"));
                        }
                    }
                }

                @Override
                public void onFailure(Exception exception) {
                    callback.onFailure(new RuntimeException("All text recognizers failed"));
                }
            });
        } else {
            callback.onFailure(new RuntimeException("No suitable text recognizer available"));
        }
    }

    /**
     * Recognize text with a specific recognizer
     */
    private void recognizeWithSpecificRecognizer(InputImage inputImage, TextRecognizer recognizer,
                                                 String recognizerName, TextRecognitionCallback callback) {
        try {
            Log.d(TAG, "Trying " + recognizerName + " recognizer...");

            Task<Text> task = recognizer.process(inputImage);

            task.addOnSuccessListener(result -> {
                try {
                    String recognizedText = result.getText();
                    Log.d(TAG, recognizerName + " recognizer result: '" + recognizedText + "'");

                    if (recognizedText == null || recognizedText.trim().isEmpty()) {
                        Log.d(TAG, "No text detected with " + recognizerName + " recognizer");
                        callback.onFailure(new RuntimeException("No text detected"));
                        return;
                    }

                    callback.onSuccess(recognizedText);

                } catch (Exception e) {
                    Log.e(TAG, "Error processing " + recognizerName + " recognition result", e);
                    callback.onFailure(e);
                }

            }).addOnFailureListener(e -> {
                Log.e(TAG, recognizerName + " text recognition failed", e);
                callback.onFailure(e);
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in " + recognizerName + " text recognition", e);
            callback.onFailure(e);
        }
    }

    /**
     * Enhance bitmap for better OCR results
     */
    private Bitmap enhanceBitmapForOCR(Bitmap originalBitmap) {
        try {
            // Create a copy to avoid modifying the original
            Bitmap enhancedBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);

            // Scale up small images for better recognition
            int width = enhancedBitmap.getWidth();
            int height = enhancedBitmap.getHeight();

            if (width < 800 || height < 600) {
                float scale = Math.max(800f / width, 600f / height);
                int newWidth = (int) (width * scale);
                int newHeight = (int) (height * scale);

                enhancedBitmap = Bitmap.createScaledBitmap(enhancedBitmap, newWidth, newHeight, true);
                Log.d(TAG, "Scaled bitmap from " + width + "x" + height + " to " + newWidth + "x" + newHeight);
            }

            return enhancedBitmap;
        } catch (Exception e) {
            Log.w(TAG, "Error enhancing bitmap, using original", e);
            return originalBitmap;
        }
    }

    /**
     * Check if recognition result is good enough
     */
    private boolean isGoodRecognitionResult(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        String cleanText = text.trim();

        // Check minimum length
        if (cleanText.length() < MIN_TEXT_LENGTH) {
            return false;
        }

        // Check if text contains meaningful characters (not just symbols/numbers)
        String alphaNumeric = cleanText.replaceAll("[^\\p{L}\\p{N}]", "");
        return alphaNumeric.length() >= Math.min(3, cleanText.length() / 2);
    }

    private boolean isValidBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e(TAG, "Bitmap is null");
            return false;
        }

        if (bitmap.isRecycled()) {
            Log.e(TAG, "Bitmap is recycled");
            return false;
        }

        if (bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
            Log.e(TAG, "Bitmap has invalid dimensions: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            return false;
        }

        if (bitmap.getWidth() > 4096 || bitmap.getHeight() > 4096) {
            Log.w(TAG, "Bitmap very large: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            // Don't reject, but log warning
        }

        Log.d(TAG, "Bitmap is valid: " + bitmap.getWidth() + "x" + bitmap.getHeight() +
                ", Config: " + bitmap.getConfig() + ", Bytes: " + bitmap.getByteCount());
        return true;
    }

    private String cleanupRecognizedText(String text) {
        if (text == null) return "";

        return text
                .trim()
                .replaceAll("\\s+", " ") // Replace multiple whitespaces with single space
                .replaceAll("[\\r\\n]+", "\n") // Normalize line breaks
                .replaceAll("\\n{3,}", "\n\n") // Limit consecutive line breaks
                .replaceAll("[\\x00-\\x1F\\x7F]", ""); // Remove control characters
    }

    private String handleRecognitionError(Exception exception) {
        String message = exception.getMessage();
        if (message == null) {
            message = exception.getClass().getSimpleName();
        }

        Log.e(TAG, "Recognition error details: " + message);

        if (message.toLowerCase().contains("timeout")) {
            return "Text recognition timed out";
        } else if (message.toLowerCase().contains("memory")) {
            return "Memory error during text recognition";
        } else if (message.toLowerCase().contains("network")) {
            return "Network error during text recognition";
        } else if (message.toLowerCase().contains("service")) {
            return "Text recognition service unavailable";
        } else {
            return "Text recognition failed: " + message;
        }
    }

    public void close() {
        try {
            if (latinTextRecognizer != null) {
                latinTextRecognizer.close();
            }
            if (chineseTextRecognizer != null) {
                chineseTextRecognizer.close();
            }
            if (devanagariTextRecognizer != null) {
                devanagariTextRecognizer.close();
            }
            if (japaneseTextRecognizer != null) {
                japaneseTextRecognizer.close();
            }
            if (koreanTextRecognizer != null) {
                koreanTextRecognizer.close();
            }
            Log.d(TAG, "TextRecognitionService closed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error closing TextRecognitionService", e);
        }
    }
}
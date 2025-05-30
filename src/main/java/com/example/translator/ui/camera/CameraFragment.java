package com.example.translator.ui.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.vision.common.InputImage;
import com.example.translator.R;
import com.example.translator.TranslatorApplication;
import com.example.translator.ui.text.LanguageSpinnerAdapter;
import com.example.translator.ui.text.LanguageSpinnerAdapter.LanguageSpinnerItem;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ExperimentalGetImage
public class CameraFragment extends Fragment {

    private static final String TAG = "CameraFragment";
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};
    private static final int CAMERA_PERMISSION_CODE = 100;

    private CameraViewModel viewModel;
    private ExecutorService cameraExecutor;

    // Views
    private PreviewView previewView;
    private Spinner spinnerSourceLanguage;
    private Spinner spinnerTargetLanguage;
    private TextView tvDetectedText;
    private TextView tvTranslatedText;
    private FloatingActionButton btnCapture;
    private ImageButton btnSwapLanguages;
    private ImageButton btnFlash;
    private ProgressBar progressBar;

    // Camera components
    private ImageCapture imageCapture;
    private ImageAnalysis imageAnalyzer;
    private Camera camera;
    private ProcessCameraProvider cameraProvider;
    private int flashMode = ImageCapture.FLASH_MODE_OFF;

    // Processing control
    private long lastProcessTime = 0L;
    private static final long PROCESSING_INTERVAL = 2000L; // 2 seconds between processing

    public CameraFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupViewModel();
        setupClickListeners();
        observeViewModel();

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissions();
        }
    }

    private void initializeViews(View view) {
        previewView = view.findViewById(R.id.preview_view);
        spinnerSourceLanguage = view.findViewById(R.id.spinner_source_language);
        spinnerTargetLanguage = view.findViewById(R.id.spinner_target_language);
        tvDetectedText = view.findViewById(R.id.tv_detected_text);
        tvTranslatedText = view.findViewById(R.id.tv_translated_text);
        btnCapture = view.findViewById(R.id.btn_capture);
        btnSwapLanguages = view.findViewById(R.id.btn_swap_languages);
        btnFlash = view.findViewById(R.id.btn_flash);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupViewModel() {
        TranslatorApplication application = (TranslatorApplication) requireActivity().getApplication();
        CameraViewModel.CameraViewModelFactory factory = new CameraViewModel.CameraViewModelFactory(
                application.getUserRepository(),
                application.getLanguageRepository(),
                requireContext()
        );
        viewModel = new ViewModelProvider(this, factory).get(CameraViewModel.class);
    }

    private void setupClickListeners() {
        btnCapture.setOnClickListener(v -> captureHighQualityImage());

        btnSwapLanguages.setOnClickListener(v -> swapLanguages());

        btnFlash.setOnClickListener(v -> toggleFlash());
    }

    private void observeViewModel() {
        viewModel.supportedLanguages.observe(getViewLifecycleOwner(), languages -> {
            // Lọc các ngôn ngữ hỗ trợ camera translation và text translation
            List<com.example.translator.data.model.Language> supportedLanguages = languages.stream()
                    .filter(lang -> lang.getSupportsCameraTranslation() || lang.getSupportsTextTranslation())
                    .collect(java.util.stream.Collectors.toList());
            setupLanguageSpinners(supportedLanguages);
        });

        viewModel.detectedText.observe(getViewLifecycleOwner(), text -> {
            String displayText = text != null ? text : getString(R.string.no_text_detected);
            tvDetectedText.setText(displayText);
        });

        viewModel.translationResult.observe(getViewLifecycleOwner(), result -> {
            String displayText = result != null ? result : getString(R.string.translation_result);
            tvTranslatedText.setText(displayText);
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                viewModel.clearError();
            }
        });
    }

    private void setupLanguageSpinners(List<com.example.translator.data.model.Language> languages) {
        if (languages.isEmpty()) return;

        LanguageSpinnerAdapter adapter = new LanguageSpinnerAdapter(requireContext(), languages);

        spinnerSourceLanguage.setAdapter(adapter);
        spinnerTargetLanguage.setAdapter(adapter);

        // Thêm option "Auto-detect" cho source language
        String[] sourceOptions = new String[languages.size() + 1];
        sourceOptions[0] = "Auto-detect";
        for (int i = 0; i < languages.size(); i++) {
            sourceOptions[i + 1] = languages.get(i).getLanguageName() + " (" + languages.get(i).getNativeName() + ")";
        }

        // Set default selections
        int defaultSourceIndex = 0; // Auto-detect
        int defaultTargetIndex = -1;

        for (int i = 0; i < languages.size(); i++) {
            if ("vi".equals(languages.get(i).getLanguageCode())) {
                defaultTargetIndex = i;
                break;
            }
        }

        spinnerSourceLanguage.setSelection(defaultSourceIndex);
        if (defaultTargetIndex != -1) spinnerTargetLanguage.setSelection(defaultTargetIndex);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (Exception exc) {
                Log.e(TAG, "Use case binding failed", exc);
                showError("Camera initialization failed");
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null) return;

        Preview preview = new Preview.Builder()
                .setTargetResolution(new Size(1920, 1080)) // HD resolution
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Cấu hình ImageCapture với chất lượng cao
        imageCapture = new ImageCapture.Builder()
                .setTargetResolution(new Size(1920, 1080))
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setFlashMode(flashMode)
                .setJpegQuality(95) // Chất lượng JPEG cao
                .build();

        imageAnalyzer = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalyzer.setAnalyzer(cameraExecutor, this::processImageSafely);

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll();

            // Bind use cases to camera
            camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer
            );

        } catch (Exception exc) {
            Log.e(TAG, "Use case binding failed", exc);
            showError("Camera binding failed");
        }
    }

    private void captureHighQualityImage() {
        if (imageCapture == null) {
            Log.e(TAG, "ImageCapture is null");
            return;
        }

        // Tạo file tạm để lưu ảnh
        String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(new Date());
        File photoFile = new File(requireContext().getExternalFilesDir(null), name + ".jpg");

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onError(ImageCaptureException exception) {
                        Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
                        showError("Failed to capture image");
                    }

                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults output) {
                        try {
                            // Đọc ảnh từ file và xử lý
                            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                            if (bitmap != null) {
                                processHighQualityImage(bitmap);
                            } else {
                                showError("Failed to load captured image");
                            }

                            // Xóa file tạm
                            if (photoFile.exists()) {
                                photoFile.delete();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing captured image", e);
                            showError("Failed to process captured image");
                        }
                    }
                }
        );
    }

    private void processHighQualityImage(Bitmap bitmap) {
        try {
            // Tạo InputImage từ bitmap chất lượng cao
            InputImage image = InputImage.fromBitmap(bitmap, 0);

            String sourceLanguage = getSelectedSourceLanguageCode();
            String targetLanguage = getSelectedTargetLanguageCode();

            Log.d(TAG, "Processing high quality image: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            Log.d(TAG, "Translation: " + sourceLanguage + " -> " + targetLanguage);

            cameraExecutor.execute(() -> {
                viewModel.recognizeText(image, new CameraViewModel.TextRecognitionCallback() {
                    @Override
                    public void onSuccess(String detectedText) {
                        if (detectedText != null && !detectedText.isEmpty()) {
                            // Nếu source language là auto-detect, detect language trước
                            if ("auto".equals(sourceLanguage)) {
                                viewModel.detectAndTranslateText(detectedText, targetLanguage);
                            } else {
                                viewModel.translateDetectedText(detectedText, sourceLanguage, targetLanguage);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        Log.e(TAG, "High quality image processing failed", exception);
                    }
                });
            });
        } catch (Exception e) {
            Log.e(TAG, "Error creating InputImage from bitmap", e);
            showError("Failed to process image");
        }
    }

    private void processImageSafely(ImageProxy imageProxy) {
        long currentTime = System.currentTimeMillis();

        // Throttle processing to avoid overwhelming the system
        if (currentTime - lastProcessTime < PROCESSING_INTERVAL) {
            imageProxy.close();
            return;
        }

        lastProcessTime = currentTime;

        try {
            Image mediaImage = imageProxy.getImage();
            if (mediaImage != null) {
                InputImage image = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.getImageInfo().getRotationDegrees()
                );

                // Process on background thread
                cameraExecutor.execute(() -> {
                    try {
                        String sourceLanguage = getSelectedSourceLanguageCode();
                        String targetLanguage = getSelectedTargetLanguageCode();

                        viewModel.recognizeText(image, new CameraViewModel.TextRecognitionCallback() {
                            @Override
                            public void onSuccess(String detectedText) {
                                if (detectedText != null && !detectedText.isEmpty()) {
                                    // Nếu source language là auto-detect, detect language trước
                                    if ("auto".equals(sourceLanguage)) {
                                        viewModel.detectAndTranslateText(detectedText, targetLanguage);
                                    } else {
                                        viewModel.translateDetectedText(detectedText, sourceLanguage, targetLanguage);
                                    }
                                }
                                imageProxy.close();
                            }

                            @Override
                            public void onFailure(Exception exception) {
                                Log.e(TAG, "Image processing failed", exception);
                                imageProxy.close();
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing image", e);
                        imageProxy.close();
                    }
                });
            } else {
                Log.w(TAG, "MediaImage is null");
                imageProxy.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing image", e);
            imageProxy.close();
        }
    }

    private void swapLanguages() {
        // Không swap nếu source là auto-detect
        if (spinnerSourceLanguage.getSelectedItemPosition() == 0) {
            Toast.makeText(requireContext(), "Cannot swap when using auto-detect", Toast.LENGTH_SHORT).show();
            return;
        }

        int sourcePosition = spinnerSourceLanguage.getSelectedItemPosition();
        int targetPosition = spinnerTargetLanguage.getSelectedItemPosition();

        spinnerSourceLanguage.setSelection(targetPosition + 1); // +1 vì có auto-detect option
        spinnerTargetLanguage.setSelection(sourcePosition - 1); // -1 vì source có auto-detect option
    }

    private void toggleFlash() {
        flashMode = (flashMode == ImageCapture.FLASH_MODE_OFF) ?
                ImageCapture.FLASH_MODE_ON : ImageCapture.FLASH_MODE_OFF;

        // Update flash icon
        int flashIcon = (flashMode == ImageCapture.FLASH_MODE_ON) ?
                android.R.drawable.ic_menu_day : android.R.drawable.ic_menu_day;
        btnFlash.setImageResource(flashIcon);

        // Rebuild camera with new flash setting
        bindCameraUseCases();
    }

    private String getSelectedSourceLanguageCode() {
        try {
            if (spinnerSourceLanguage.getSelectedItemPosition() == 0) {
                return "auto"; // Auto-detect
            }

            LanguageSpinnerItem item = (LanguageSpinnerItem) spinnerSourceLanguage.getSelectedItem();
            return item != null ? item.language.getLanguageCode() : "auto";
        } catch (Exception e) {
            return "auto";
        }
    }

    private String getSelectedTargetLanguageCode() {
        try {
            LanguageSpinnerItem item = (LanguageSpinnerItem) spinnerTargetLanguage.getSelectedItem();
            return item != null ? item.language.getLanguageCode() : "vi";
        } catch (Exception e) {
            return "vi";
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, CAMERA_PERMISSION_CODE);
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                showError(getString(R.string.camera_permission_required));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (allPermissionsGranted() && cameraProvider == null) {
            startCamera();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Clean up camera resources
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            camera = null;
            cameraProvider = null;
        }

        // Shutdown executor
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}
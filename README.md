# Translator App - Ứng dụng Dịch thuật cho Android

## 📱 Giới thiệu

**Translator App** là một ứng dụng dịch thuật hiện đại, hỗ trợ nhiều phương thức dịch khác nhau bao gồm:
- Dịch văn bản (Text Translation)
- Dịch từ hình ảnh (Image Translation)
- Dịch trực tiếp bằng camera (Camera Translation)
- Dịch giọng nói (Voice Translation)

Ứng dụng được phát triển trên nền tảng **Android**, sử dụng kiến trúc **MVVM** và các công nghệ như **Google ML Kit**, **OCR**, **Text-to-Speech**, và **Speech Recognition** nhằm cung cấp trải nghiệm dịch thuật nhanh chóng, chính xác và tiện lợi cho người dùng.

---

## 🔧 Công nghệ sử dụng

- **Android SDK** (Java)
- **ML Kit**: OCR, Text Translation, Language Identification
- **Camera API**
- **Text-to-Speech Engine**
- **Speech Recognition API**
- **MVVM Architecture**
- **Offline Caching** *(nếu có)*

---

## 🧩 Chức năng chính

### 1. Dịch văn bản
- Nhập văn bản cần dịch
- Chọn ngôn ngữ nguồn và đích
- Nhấn "Translate" để xem kết quả

### 2. Dịch giọng nói
- Ghi âm bằng micro
- Chuyển đổi giọng nói thành văn bản
- Dịch và hiển thị kết quả

### 3. Dịch hình ảnh
- Chọn ảnh từ thư viện
- Cắt vùng chứa văn bản
- Nhận diện văn bản bằng OCR
- Dịch và hiển thị kết quả

### 4. Dịch trực tiếp qua camera
- Mở camera
- Quét văn bản thời gian thực
- Dịch và hiển thị kết quả trên màn hình

### 5. Đọc văn bản (Text-to-Speech)
- Hỗ trợ đọc nội dung đã dịch bằng giọng nói

### 6. Tóm tắt nội dung
- Tóm tắt văn bản với 4 chế độ:
    - Brief Summary
    - Detailed Summary
    - Key Points
    - Key Phrases

### 7. Cài đặt
- Chọn ngôn ngữ mặc định
- Giao diện Light/Dark
- Cấu hình Text-to-Speech
- Tự động nhận diện ngôn ngữ
- Quản lý quyền truy cập (camera, micro, mạng)

---

## 📂 Cấu trúc thư mục

```bash
src/
└── main/
    ├── AndroidManifest.xml
    ├── java/com/example/translator/
    │   ├── TranslatorApplication.java
    │   ├── data/                # Dữ liệu: model, dao, repository
    │   ├── services/            # Dịch vụ: ML Kit, Speech, Tóm tắt
    │   ├── ui/                  # Giao diện người dùng: activity/fragment/viewmodel
    │   └── utils/               # Tiện ích
    └── res/                     # Tài nguyên: drawable, layout, mipmap, values, xml
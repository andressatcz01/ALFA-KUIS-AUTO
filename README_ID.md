# ALFA KUIS AUTO v2 — Android + Web Admin

## Fitur versi ini
- Web Admin untuk membuat, mengedit, mencari, menghapus, impor, dan ekspor bank soal.
- Impor Web Admin: JSON, CSV/TXT, XLSX/XLS (XLSX memakai SheetJS dari CDN, perlu internet saat membuka admin).
- Tipe soal: pilihan ganda, multi-select, benar/salah, jawaban singkat, esai, menjodohkan, mengurutkan, berbasis gambar, dan berbasis audio.
- Ekspor bank menjadi `alfa-kuis-bank.json`.
- Android: impor JSON hasil ekspor Web Admin, OCR screenshot soal dengan ML Kit, pencarian kandidat soal, kunci dan pembahasan.
- GitHub Actions membangun APK debug dan menyimpan APK sebagai artifact.

## Struktur folder
- `web-admin/index.html` — panel admin statis.
- `android/` — proyek Android Kotlin.
- `.github/workflows/build-android.yml` — workflow build APK.

## Cara menggunakan Web Admin dari HP
1. Buka `web-admin/index.html` di browser atau unggah folder `web-admin` ke layanan hosting statis.
2. Tambahkan soal atau impor file CSV/JSON/XLSX.
3. Klik **Ekspor JSON untuk Android**. Simpan file `alfa-kuis-bank.json`.
4. Kirim file JSON ke HP bila admin dibuka di perangkat lain.

## Format JSON
```json
{
  "schemaVersion": 1,
  "packageName": "Bank Soal AlfaLearning",
  "questions": [
    {
      "id": "q001",
      "type": "single_choice",
      "category": "Produk",
      "question": "Contoh pertanyaan?",
      "options": ["A. Pilihan 1", "B. Pilihan 2"],
      "answer": "A. Pilihan 1",
      "explanation": "Pembahasan singkat",
      "image": "",
      "audio": ""
    }
  ]
}
```

## Cara membuat APK langsung dari GitHub lewat Android
1. Unggah isi folder proyek ini ke repository `andressatcz01/ALFA-KUIS-AUTO` (folder `.github` harus ikut terunggah).
2. Buka tab **Actions** → workflow **Build ALFA KUIS AUTO APK**.
3. Tekan **Run workflow** dan tunggu sampai status hijau.
4. Buka hasil workflow yang berhasil → bagian **Artifacts** → unduh `alfa-kuis-auto-debug-apk`.
5. Ekstrak ZIP artifact dan instal `app-debug.apk` di Android.

## Perbaikan error build sebelumnya
- Java dan Kotlin sama-sama ditargetkan ke JVM 17 (`sourceCompatibility`, `targetCompatibility`, `jvmTarget`).
- Workflow tidak meminta paket Android SDK lama bernama `tools`; paket yang dipasang hanya `platform-tools`, `platforms;android-35`, dan `build-tools;35.0.0`.
- Build memakai Gradle 8.9 yang sesuai dengan Android Gradle Plugin 8.7.3.

## Batas penggunaan
Aplikasi ini adalah asisten belajar: membaca screenshot yang dipilih pengguna dan menampilkan kandidat dari bank soal. Aplikasi tidak membaca layar secara diam-diam, tidak mengklik pilihan, dan tidak mengirim/menyelesaikan kuis otomatis di AlfaLearning. Untuk integrasi resmi ke `alfalearning.sat.co.id`, diperlukan API atau izin integrasi dari pengelola platform.

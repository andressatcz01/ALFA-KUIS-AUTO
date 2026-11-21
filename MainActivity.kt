package id.alfaku.auto

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private val pickImage = 1001
    private val pickBank = 1002
    private lateinit var questionText: TextView
    private lateinit var resultText: TextView
    private lateinit var countText: TextView
    private val bank = mutableListOf<JSONObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "ALFA KUIS AUTO"
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(22, 26, 22, 24); setBackgroundColor(0xFFF4F6FA.toInt()) }
        root.addView(TextView(this).apply { text = "ALFA KUIS AUTO\nAsisten Belajar & Bank Soal"; textSize = 24f; setTextColor(0xFFC8102E.toInt()) })
        root.addView(TextView(this).apply { text = "Impor bank soal dari Web Admin (JSON), baca screenshot dengan OCR, lalu lihat kandidat jawaban dan pembahasan. Aplikasi tidak mengklik atau mengirim jawaban ke kuis aktif."; textSize = 13f; setPadding(0, 12, 0, 14) })
        val import = Button(this).apply { text = "IMPOR BANK SOAL JSON" }; root.addView(import)
        countText = TextView(this).apply { text = "Bank soal: 0"; textSize = 14f }; root.addView(countText)
        val scan = Button(this).apply { text = "PILIH SCREENSHOT SOAL (OCR)" }; root.addView(scan)
        questionText = TextView(this).apply { text = "Teks soal akan muncul di sini."; textSize = 16f; setPadding(12, 16, 12, 16); setBackgroundColor(0xFFFFFFFF.toInt()) }; root.addView(questionText)
        val match = Button(this).apply { text = "CARI KECocokan BANK SOAL" }; root.addView(match)
        resultText = TextView(this).apply { text = "Kandidat jawaban dan pembahasan tampil di sini."; textSize = 15f; setPadding(12, 16, 12, 16); setBackgroundColor(0xFFE8F0FA.toInt()) }; root.addView(resultText)
        val scroll = ScrollView(this).apply { addView(root) }; setContentView(scroll)
        import.setOnClickListener { val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply { addCategory(Intent.CATEGORY_OPENABLE); type = "application/json" }; startActivityForResult(i, pickBank) }
        scan.setOnClickListener { val i = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }; startActivityForResult(Intent.createChooser(i, "Pilih screenshot soal"), pickImage) }
        match.setOnClickListener { matchQuestion() }
    }

    private fun loadBank(uri: Uri) {
        try {
            val raw = contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: error("File kosong")
            val obj = JSONObject(raw)
            val arr = obj.optJSONArray("questions") ?: JSONArray(raw)
            bank.clear()
            for (i in 0 until arr.length()) bank.add(arr.getJSONObject(i))
            countText.text = "Bank soal: ${bank.size}"
            Toast.makeText(this, "Bank soal berhasil diimpor", Toast.LENGTH_LONG).show()
        } catch (e: Exception) { Toast.makeText(this, "Gagal membaca JSON: ${e.message}", Toast.LENGTH_LONG).show() }
    }

    private fun matchQuestion() {
        val q = questionText.text.toString().lowercase()
        if (bank.isEmpty()) { resultText.text = "Bank soal belum diimpor. Ekspor JSON dari Web Admin terlebih dahulu."; return }
        val hits = bank.mapNotNull { item ->
            val question = item.optString("question")
            val words = question.lowercase().split(Regex("[^\\p{L}\\p{N}]+")).filter { it.length > 3 }.distinct()
            val score = words.count { q.contains(it) }
            if (score > 0) Triple(item, score, words.size) else null
        }.sortedByDescending { it.second }.take(5)
        resultText.text = if (hits.isEmpty()) "Belum ada soal yang cocok. Periksa kualitas screenshot atau tambahkan variasi kata kunci pada bank." else hits.joinToString("\n\n──────────────\n\n") { (item, score, total) ->
            "Jenis: ${item.optString("type", "soal")}  |  Kecocokan kata: $score/$total\nSoal bank: ${item.optString("question")}\nKunci: ${item.optString("answer")}\nPembahasan: ${item.optString("explanation", "Tidak tersedia")}" }
    }

    @Deprecated("Deprecated API retained for compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        val uri = data?.data ?: return
        if (requestCode == pickBank) { loadBank(uri); return }
        if (requestCode == pickImage) try {
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val image = InputImage.fromBitmap(bitmap, 0)
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS).process(image)
                .addOnSuccessListener { questionText.text = it.text.ifBlank { "Tidak ada teks terbaca. Coba screenshot lebih jelas." } }
                .addOnFailureListener { questionText.text = "OCR gagal membaca gambar." }
        } catch (_: Exception) { questionText.text = "Gambar tidak dapat dibuka." }
    }
}

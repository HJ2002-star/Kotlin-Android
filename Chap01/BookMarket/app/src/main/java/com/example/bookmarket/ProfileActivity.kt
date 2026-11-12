package com.example.bookmarket

import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var profileIdInput: EditText

    private val preferences by lazy {
        getSharedPreferences("profile_settings", MODE_PRIVATE)
    }

    private val photoFile: File
        get() = File(filesDir, "profile_photo.jpg")

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            savePhoto(bitmap)
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            loadGalleryPhoto(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        val root = findViewById<View>(R.id.profileRoot)

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                bars.left,
                bars.top,
                bars.right,
                bars.bottom
            )

            insets
        }

        val toolbar = findViewById<Toolbar>(R.id.profileToolbar)

        toolbar.setNavigationIcon(
            androidx.appcompat.R.drawable.abc_ic_ab_back_material
        )
        toolbar.navigationIcon?.setTint(Color.WHITE)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        profileImage = findViewById(R.id.profileImage)
        profileIdInput = findViewById(R.id.profileIdInput)

        profileIdInput.setText(
            preferences.getString("profile_id", "")
        )

        profileImage.outlineProvider =
            object : ViewOutlineProvider() {
                override fun getOutline(
                    view: View,
                    outline: Outline
                ) {
                    outline.setOval(
                        0,
                        0,
                        view.width,
                        view.height
                    )
                }
            }

        profileImage.clipToOutline = true

        val changeButton = findViewById<ImageButton>(
            R.id.changeProfileButton
        )

        changeButton.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor("#459E9E"))
        }

        changeButton.backgroundTintList = null

        changeButton.setOnClickListener {
            showPhotoChoices()
        }

        findViewById<Button>(
            R.id.openMemoButton
        ).setOnClickListener {
            startActivity(
                android.content.Intent(
                    this,
                    MemoActivity::class.java
                )
            )
        }

        showSavedPhoto()
    }

    private fun showPhotoChoices() {
        val choices = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(12), dp(24), dp(20))
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("선택하세요")
            .setView(choices)
            .create()

        fun addChoice(
            label: String,
            icon: Int,
            action: () -> Unit
        ) {
            val column = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                isClickable = true
                isFocusable = true
                contentDescription = label

                setOnClickListener {
                    dialog.dismiss()
                    action()
                }
            }

            column.addView(
                ImageView(this).apply {
                    setImageResource(icon)
                    setColorFilter(Color.parseColor("#459E9E"))
                    scaleType = ImageView.ScaleType.FIT_CENTER
                },
                LinearLayout.LayoutParams(
                    dp(52),
                    dp(52)
                )
            )

            column.addView(
                TextView(this).apply {
                    text = label
                    textSize = 14f
                    gravity = Gravity.CENTER
                    setTextColor(Color.parseColor("#555555"))
                    setPadding(0, dp(8), 0, 0)
                }
            )

            choices.addView(
                column,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )
        }

        addChoice(
            "카메라",
            android.R.drawable.ic_menu_camera
        ) {
            try {
                cameraLauncher.launch(null)
            } catch (error: ActivityNotFoundException) {
                showMessage("사용 가능한 카메라 앱이 없습니다")
            } catch (error: SecurityException) {
                showMessage("카메라 실행 권한을 확인하세요")
            }
        }

        addChoice(
            "갤러리",
            android.R.drawable.ic_menu_gallery
        ) {
            try {
                galleryLauncher.launch("image/*")
            } catch (error: ActivityNotFoundException) {
                showMessage("사용 가능한 사진 선택 앱이 없습니다")
            }
        }

        dialog.show()
    }

    private fun loadGalleryPhoto(uri: Uri) {
        try {
            val bitmap = if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
            ) {
                val source = ImageDecoder.createSource(
                    contentResolver,
                    uri
                )

                ImageDecoder.decodeBitmap(source) {
                        decoder, info, _ ->
                    decoder.allocator =
                        ImageDecoder.ALLOCATOR_SOFTWARE

                    val width = info.size.width
                    val height = info.size.height
                    val scale = max(width, height) / 1280f

                    if (scale > 1f) {
                        decoder.setTargetSize(
                            (width / scale).roundToInt()
                                .coerceAtLeast(1),
                            (height / scale).roundToInt()
                                .coerceAtLeast(1)
                        )
                    }
                }
            } else {
                val bounds = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }

                contentResolver.openInputStream(uri).use {
                    BitmapFactory.decodeStream(
                        it,
                        null,
                        bounds
                    )
                }

                var sampleSize = 1

                while (
                    max(
                        bounds.outWidth / sampleSize,
                        bounds.outHeight / sampleSize
                    ) > 1280
                ) {
                    sampleSize *= 2
                }

                val options = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                }

                contentResolver.openInputStream(uri).use {
                    BitmapFactory.decodeStream(
                        it,
                        null,
                        options
                    )
                }
            }

            if (bitmap == null) {
                showMessage("사진을 읽을 수 없습니다")
            } else {
                savePhoto(bitmap)
            }
        } catch (error: Exception) {
            showMessage("사진을 불러오지 못했습니다")
        }
    }

    private fun savePhoto(bitmap: Bitmap) {
        try {
            val temporaryFile = File(
                filesDir,
                "profile_photo_pending.jpg"
            )

            temporaryFile.outputStream().use { output ->
                check(
                    bitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        90,
                        output
                    )
                )
            }

            temporaryFile.copyTo(
                photoFile,
                overwrite = true
            )
            temporaryFile.delete()

            profileImage.setImageBitmap(bitmap)
            showMessage("프로필 사진을 저장했습니다")
        } catch (error: Exception) {
            showMessage("사진을 저장하지 못했습니다")
        }
    }

    private fun showSavedPhoto() {
        val savedPhoto = if (photoFile.exists()) {
            BitmapFactory.decodeFile(
                photoFile.absolutePath
            )
        } else {
            null
        }

        profileImage.setImageBitmap(
            savedPhoto ?: createDefaultProfile()
        )
    }

    private fun createDefaultProfile(): Bitmap {
        val bitmap = Bitmap.createBitmap(
            240,
            240,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#464E70")
        }

        canvas.drawCircle(
            120f,
            120f,
            120f,
            paint
        )

        paint.color = Color.parseColor("#E6E8EF")

        canvas.drawCircle(
            120f,
            88f,
            43f,
            paint
        )

        canvas.drawOval(
            43f,
            133f,
            197f,
            276f,
            paint
        )

        return bitmap
    }

    override fun onPause() {
        if (::profileIdInput.isInitialized) {
            preferences.edit()
                .putString(
                    "profile_id",
                    profileIdInput.text.toString().trim()
                )
                .apply()
        }

        super.onPause()
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
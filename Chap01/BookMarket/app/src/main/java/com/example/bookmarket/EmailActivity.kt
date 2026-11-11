package com.example.bookmarket

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EmailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_email)

        val main = findViewById<View>(R.id.main)
        val spacing = (20 * resources.displayMetrics.density).toInt()

        ViewCompat.setOnApplyWindowInsetsListener(main) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )
            view.setPadding(
                spacing + bars.left,
                spacing + bars.top,
                spacing + bars.right,
                spacing + bars.bottom
            )
            insets
        }

        val recipient = findViewById<EditText>(R.id.recipientInput)
        val subject = findViewById<EditText>(R.id.subjectInput)
        val message = findViewById<EditText>(R.id.messageInput)

        findViewById<Button>(R.id.openEmailButton).setOnClickListener {
            val address = recipient.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(address).matches()) {
                recipient.error = "이메일 주소를 확인하세요"
                return@setOnClickListener
            }

            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
                putExtra(Intent.EXTRA_SUBJECT, subject.text.toString())
                putExtra(Intent.EXTRA_TEXT, message.text.toString())
            }

            try {
                startActivity(
                    Intent.createChooser(emailIntent, "메일 앱 선택")
                )
            } catch (error: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "사용 가능한 메일 앱이 없습니다",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
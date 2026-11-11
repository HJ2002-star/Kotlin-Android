package com.example.bookmarket

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_count)

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

        val turtleInput = findViewById<EditText>(R.id.turtleCount)
        val fishInput = findViewById<EditText>(R.id.fishCount)

        findViewById<Button>(
            R.id.checkAnswerButton
        ).setOnClickListener {
            val turtles = turtleInput.text.toString().toIntOrNull()
            val fish = fishInput.text.toString().toIntOrNull()

            val message = when {
                turtles == null || fish == null ->
                    "그림의 개수를 모두 입력하세요"

                turtles == 2 && fish == 5 ->
                    "맞았습니다"

                else ->
                    "틀렸습니다"
            }

            Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
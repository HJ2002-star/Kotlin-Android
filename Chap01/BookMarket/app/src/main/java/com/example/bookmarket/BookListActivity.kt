package com.example.bookmarket

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class BookListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_book_list)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById<View>(R.id.main)
        ) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )
            view.setPadding(
                bars.left, bars.top, bars.right, bars.bottom
            )
            insets
        }

        val books = mapOf(
            R.id.algorithmBook to "쉽게 배우는 알고리즘",
            R.id.mysqlBook to "처음 시작하는 MySQL",
            R.id.phpBook to "쉽게 배우는 PHP 웹 프로그래밍",
            R.id.webBook to "미라클 HTML5 + CSS3 + 자바스크립트"
        )

        books.forEach { (imageId, title) ->
            findViewById<ImageView>(imageId).setOnClickListener {
                Toast.makeText(
                    this,
                    title,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
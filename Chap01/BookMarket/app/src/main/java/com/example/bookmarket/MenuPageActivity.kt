package com.example.bookmarket

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MenuPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_page)

        val pageTitle = intent.getStringExtra("pageTitle")
            ?: "BookMarket"

        val root = findViewById<View>(R.id.menuPageRoot)

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

        val toolbar = findViewById<Toolbar>(R.id.menuPageToolbar)

        toolbar.title = pageTitle
        toolbar.setNavigationIcon(
            androidx.appcompat.R.drawable.abc_ic_ab_back_material
        )
        toolbar.navigationIcon?.setTint(Color.WHITE)
        toolbar.navigationContentDescription = "이전 화면"

        toolbar.setNavigationOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.menuPageTitle).text = pageTitle
    }
}
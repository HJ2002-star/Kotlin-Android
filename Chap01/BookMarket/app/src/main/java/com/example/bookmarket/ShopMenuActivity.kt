package com.example.bookmarket

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout

class ShopMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_shop_menu)

        val drawer = findViewById<DrawerLayout>(R.id.shopDrawer)
        val content = findViewById<View>(R.id.shopContent)
        val navigation = findViewById<View>(R.id.shopNavigation)
        val toolbar = findViewById<Toolbar>(R.id.shopToolbar)

        ViewCompat.setOnApplyWindowInsetsListener(drawer) { _, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            content.setPadding(
                bars.left,
                bars.top,
                bars.right,
                bars.bottom
            )

            navigation.setPadding(
                0,
                bars.top,
                0,
                bars.bottom
            )

            WindowInsetsCompat.CONSUMED
        }

        toolbar.setNavigationOnClickListener {
            drawer.openDrawer(GravityCompat.START)
        }

        findViewById<View>(R.id.openBooksButton).setOnClickListener {
            startActivity(
                Intent(this, BookListActivity::class.java)
            )
        }

        val menuLabels = mapOf(
            R.id.ebookButton to "E-Book",
            R.id.customerButton to "고객센터",
            R.id.myPageButton to "나의페이지",
            R.id.marketButton to "마켓정보",
            R.id.settingsButton to "설정",
            R.id.navLogin to "로그인",
            R.id.navSettings to "환경설정",
            R.id.navMyInfo to "나의정보"
        )

        menuLabels.forEach { (viewId, label) ->
            findViewById<View>(viewId).setOnClickListener {
                drawer.closeDrawer(GravityCompat.START)

                Toast.makeText(
                    this,
                    label,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (drawer.isDrawerOpen(GravityCompat.START)) {
                        drawer.closeDrawer(GravityCompat.START)
                    } else {
                        finish()
                    }
                }
            }
        )

        ViewCompat.requestApplyInsets(drawer)
    }
}
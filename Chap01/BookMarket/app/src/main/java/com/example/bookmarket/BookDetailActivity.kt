package com.example.bookmarket

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class BookDetailActivity : AppCompatActivity() {

    companion object {
        private const val MENU_CART = 201
        private const val MENU_HOME = 202
        private const val MENU_VIDEO = 203
        private const val MENU_CUSTOMER = 204
        private const val MENU_MY_PAGE = 205
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_book_detail)

        val root = findViewById<View>(R.id.detailRoot)

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )
            view.setPadding(
                bars.left, bars.top, bars.right, bars.bottom
            )
            insets
        }

        val toolbar = findViewById<Toolbar>(R.id.detailToolbar)

        toolbar.setNavigationIcon(
            androidx.appcompat.R.drawable.abc_ic_ab_back_material
        )
        toolbar.navigationIcon?.setTint(Color.WHITE)
        toolbar.navigationContentDescription = "도서목록으로 돌아가기"

        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        val title = intent.getStringExtra("bookTitle")
            ?: "쉽게 배우는 PHP 웹 프로그래밍"

        val bookId = intent.getStringExtra("bookId")
            ?: "BOOK1236"

        val price = intent.getStringExtra("bookPrice")
            ?: "30,000원"

        val cover = intent.getIntExtra(
            "bookCover",
            R.drawable.books_book3
        )

        findViewById<TextView>(R.id.detailTitle).text = title
        findViewById<TextView>(R.id.detailBookId).text = bookId
        findViewById<TextView>(R.id.detailPrice).text = price

        findViewById<ImageView>(R.id.detailCover).apply {
            setImageResource(cover)
            contentDescription = title
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(Menu.NONE, MENU_CART, 0, "장바구니").apply {
            setIcon(R.drawable.icon_cart)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

        menu.add(Menu.NONE, MENU_HOME, 1, "홈으로")
        menu.add(Menu.NONE, MENU_VIDEO, 2, "동영상강좌")
        menu.add(Menu.NONE, MENU_CUSTOMER, 3, "고객센터")
        menu.add(Menu.NONE, MENU_MY_PAGE, 4, "마이페이지")

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            MENU_HOME -> {
                showMessage("홈으로")

                startActivity(
                    Intent(this, ShopMenuActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                )

                finish()
                true
            }

            MENU_CART,
            MENU_VIDEO,
            MENU_CUSTOMER,
            MENU_MY_PAGE -> {
                val selectedTitle = item.title.toString()
                showMessage(selectedTitle)

                startActivity(
                    Intent(this, MenuPageActivity::class.java).apply {
                        putExtra("pageTitle", selectedTitle)
                    }
                )
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
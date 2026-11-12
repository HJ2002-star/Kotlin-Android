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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class BookDetailActivity : AppCompatActivity() {

    companion object {
        private const val MENU_ADD_CART = 201
        private const val MENU_HOME = 202
        private const val MENU_VIDEO = 203
        private const val MENU_CUSTOMER = 204
        private const val MENU_MY_PAGE = 205
        private const val MENU_OPEN_CART = 206
    }

    private lateinit var bookTitle: String
    private lateinit var bookId: String
    private lateinit var bookPriceText: String

    private var bookCover = R.drawable.books_book3
    private var bookPrice = 30000

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
                bars.left,
                bars.top,
                bars.right,
                bars.bottom
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

        bookTitle = intent.getStringExtra("bookTitle")
            ?: "쉽게 배우는 PHP 웹 프로그래밍"

        bookId = intent.getStringExtra("bookId")
            ?: "BOOK1236"

        bookPriceText = intent.getStringExtra("bookPrice")
            ?: "30,000원"

        bookPrice = bookPriceText
            .filter { it.isDigit() }
            .toIntOrNull()
            ?: 30000

        bookCover = intent.getIntExtra(
            "bookCover",
            R.drawable.books_book3
        )

        findViewById<TextView>(R.id.detailTitle).text =
            bookTitle

        findViewById<TextView>(R.id.detailBookId).text =
            bookId

        findViewById<TextView>(R.id.detailPrice).text =
            bookPriceText

        findViewById<ImageView>(R.id.detailCover).apply {
            setImageResource(bookCover)
            contentDescription = bookTitle
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(
            Menu.NONE,
            MENU_ADD_CART,
            0,
            "장바구니 담기"
        ).apply {
            setIcon(R.drawable.icon_cart)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

        menu.add(Menu.NONE, MENU_OPEN_CART, 1, "장바구니 보기")
        menu.add(Menu.NONE, MENU_HOME, 2, "홈으로")
        menu.add(Menu.NONE, MENU_VIDEO, 3, "동영상강좌")
        menu.add(Menu.NONE, MENU_CUSTOMER, 4, "고객센터")
        menu.add(Menu.NONE, MENU_MY_PAGE, 5, "마이페이지")

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            MENU_ADD_CART -> {
                showAddCartDialog()
                true
            }

            MENU_OPEN_CART -> {
                startActivity(
                    Intent(this, CartActivity::class.java)
                )
                true
            }

            MENU_HOME -> {
                showMessage("홈으로")

                startActivity(
                    Intent(
                        this,
                        ShopMenuActivity::class.java
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                )

                finish()
                true
            }

            MENU_VIDEO,
            MENU_CUSTOMER,
            MENU_MY_PAGE -> {
                val selectedTitle = item.title.toString()

                showMessage(selectedTitle)

                startActivity(
                    Intent(
                        this,
                        MenuPageActivity::class.java
                    ).apply {
                        putExtra("pageTitle", selectedTitle)
                    }
                )

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAddCartDialog() {
        AlertDialog.Builder(this)
            .setTitle("장바구니 담기")
            .setMessage("도서를 장바구니에 추가하겠습니까?")
            .setNegativeButton("아니오", null)
            .setPositiveButton("예") { _, _ ->
                CartStore.add(
                    bookId,
                    bookTitle,
                    bookPrice,
                    bookCover
                )

                showMessage("장바구니에 담았습니다")

                startActivity(
                    Intent(this, CartActivity::class.java)
                )
            }
            .show()
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
package com.example.bookmarket

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class BookListActivity : AppCompatActivity() {

    companion object {
        private const val MENU_HOME = 101
        private const val MENU_VIDEO = 102
        private const val MENU_CUSTOMER = 103
        private const val MENU_MY_PAGE = 104
        private const val MENU_SEARCH = 105
    }

    private data class Book(
        val viewId: Int,
        val id: String,
        val title: String,
        val price: String,
        val cover: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_book_list)

        val content = findViewById<LinearLayout>(R.id.main)

        ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
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

        val toolbar = Toolbar(this).apply {
            title = "도서목록"
            setTitleTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(69, 158, 158))

            setNavigationIcon(
                androidx.appcompat.R.drawable.abc_ic_ab_back_material
            )
            navigationIcon?.setTint(Color.WHITE)
            navigationContentDescription = "이전 화면"

            overflowIcon = ContextCompat.getDrawable(
                this@BookListActivity,
                androidx.appcompat.R.drawable.abc_ic_menu_overflow_material
            )?.mutate()?.apply {
                setTint(Color.WHITE)
            }
        }

        val toolbarHeight =
            (56 * resources.displayMetrics.density).toInt()

        content.addView(
            toolbar,
            0,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                toolbarHeight
            )
        )

        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        val books = listOf(
            Book(
                R.id.algorithmBook,
                "BOOK1234",
                "쉽게 배우는 알고리즘",
                "22,000원",
                R.drawable.books_book1
            ),
            Book(
                R.id.mysqlBook,
                "BOOK1235",
                "처음 시작하는 MySQL",
                "34,000원",
                R.drawable.books_book2
            ),
            Book(
                R.id.phpBook,
                "BOOK1236",
                "쉽게 배우는 PHP 웹 프로그래밍",
                "30,000원",
                R.drawable.books_book3
            ),
            Book(
                R.id.webBook,
                "BOOK1237",
                "미라클 HTML5 + CSS3 + 자바스크립트",
                "28,000원",
                R.drawable.books_book4
            )
        )

        books.forEach { book ->
            findViewById<ImageView>(book.viewId).setOnClickListener {
                showMessage(book.title)

                val detailIntent =
                    Intent(this, BookDetailActivity::class.java).apply {
                        putExtra("bookId", book.id)
                        putExtra("bookTitle", book.title)
                        putExtra("bookPrice", book.price)
                        putExtra("bookCover", book.cover)
                    }

                startActivity(detailIntent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(Menu.NONE, MENU_SEARCH, 0, "검색").apply {
            setIcon(R.drawable.icon_search)
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

            MENU_VIDEO,
            MENU_CUSTOMER,
            MENU_MY_PAGE,
            MENU_SEARCH -> {
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
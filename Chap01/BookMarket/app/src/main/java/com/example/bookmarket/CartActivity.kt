package com.example.bookmarket

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.NumberFormat
import java.util.Locale

class CartActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var selectAll: CheckBox
    private lateinit var totalText: TextView
    private lateinit var checkoutButton: Button

    private val selectedIds = mutableSetOf<String>()
    private val itemChecks = mutableListOf<CheckBox>()

    private var updatingChecks = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cart)

        val root = findViewById<View>(R.id.cartRoot)

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

        val toolbar = findViewById<Toolbar>(R.id.cartToolbar)

        toolbar.setNavigationIcon(
            androidx.appcompat.R.drawable.abc_ic_ab_back_material
        )
        toolbar.navigationIcon?.setTint(Color.WHITE)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        container = findViewById(R.id.cartItemsContainer)
        selectAll = findViewById(R.id.selectAllCartCheck)
        totalText = findViewById(R.id.cartTotalText)
        checkoutButton = findViewById(R.id.checkoutCartButton)

        selectAll.setOnCheckedChangeListener { _, checked ->
            if (!updatingChecks) {
                updatingChecks = true

                itemChecks.forEach {
                    it.isChecked = checked
                }

                selectedIds.clear()

                if (checked) {
                    selectedIds.addAll(
                        CartStore.items.map { it.id }
                    )
                }

                updatingChecks = false
            }
        }

        findViewById<Button>(
            R.id.deleteSelectedCartButton
        ).setOnClickListener {
            if (selectedIds.isEmpty()) {
                Toast.makeText(
                    this,
                    "삭제할 도서를 선택하세요",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                confirmDelete(selectedIds.toList())
            }
        }

        checkoutButton.setOnClickListener {
            if (CartStore.items.isNotEmpty()) {
                startActivity(
                    Intent(this, ShippingActivity::class.java)
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showCart()
    }

    private fun showCart() {
        container.removeAllViews()
        itemChecks.clear()
        selectedIds.clear()

        val items = CartStore.items

        updatingChecks = true
        selectAll.isChecked = items.isNotEmpty()
        selectAll.isEnabled = items.isNotEmpty()
        updatingChecks = false

        checkoutButton.isEnabled = items.isNotEmpty()

        totalText.text =
            "합계 : ${money(CartStore.total)}원"

        if (items.isEmpty()) {
            container.addView(
                TextView(this).apply {
                    text = "장바구니가 비어 있습니다"
                    textSize = 16f
                    gravity = Gravity.CENTER
                    setTextColor(Color.parseColor("#666666"))
                    setPadding(dp(16), dp(60), dp(16), dp(60))
                }
            )
            return
        }

        items.forEach { item ->
            selectedIds.add(item.id)

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(10)
                }

                setPadding(dp(2), dp(8), dp(2), dp(8))
            }

            val checkBox = CheckBox(this).apply {
                isChecked = true
                contentDescription = "${item.title} 선택"

                setOnCheckedChangeListener { _, checked ->
                    if (!updatingChecks) {
                        if (checked) {
                            selectedIds.add(item.id)
                        } else {
                            selectedIds.remove(item.id)
                        }

                        updatingChecks = true

                        selectAll.isChecked =
                            selectedIds.size == CartStore.items.size

                        updatingChecks = false
                    }
                }
            }

            itemChecks.add(checkBox)

            row.addView(
                checkBox,
                LinearLayout.LayoutParams(
                    dp(42),
                    dp(48)
                )
            )

            row.addView(
                ImageView(this).apply {
                    setImageResource(item.cover)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    contentDescription = item.title
                },
                LinearLayout.LayoutParams(
                    dp(62),
                    dp(92)
                )
            )

            row.addView(
                TextView(this).apply {
                    text =
                        "${item.title}\n" +
                                "가격 : ${money(item.price.toLong())}원\n" +
                                "수량 : ${item.quantity}\n" +
                                "소계 : ${money(item.subtotal)}원"

                    textSize = 12f
                    setTextColor(Color.parseColor("#333333"))
                    setPadding(dp(8), 0, dp(4), 0)
                },
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            row.addView(
                ImageButton(this).apply {
                    setImageResource(
                        android.R.drawable.ic_menu_delete
                    )
                    setBackgroundColor(Color.TRANSPARENT)
                    contentDescription = "${item.title} 삭제"

                    setOnClickListener {
                        confirmDelete(listOf(item.id))
                    }
                },
                LinearLayout.LayoutParams(
                    dp(44),
                    dp(48)
                )
            )

            container.addView(row)

            container.addView(
                View(this).apply {
                    setBackgroundColor(
                        Color.parseColor("#E5E5E5")
                    )
                },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(1)
                )
            )
        }
    }

    private fun confirmDelete(ids: List<String>) {
        AlertDialog.Builder(this)
            .setTitle("도서 삭제")
            .setMessage("선택한 도서를 삭제하겠습니까?")
            .setNegativeButton("아니오", null)
            .setPositiveButton("예") { _, _ ->
                ids.forEach {
                    CartStore.remove(it)
                }

                showCart()
            }
            .show()
    }

    private fun money(value: Long): String {
        return NumberFormat.getIntegerInstance(
            Locale.KOREA
        ).format(value)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
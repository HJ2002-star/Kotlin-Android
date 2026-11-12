package com.example.bookmarket

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.NumberFormat
import java.util.Locale

class OrderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_order)

        val root = findViewById<View>(R.id.orderRoot)

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

        val toolbar = findViewById<Toolbar>(R.id.orderToolbar)

        toolbar.setNavigationIcon(
            androidx.appcompat.R.drawable.abc_ic_ab_back_material
        )
        toolbar.navigationIcon?.setTint(Color.WHITE)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val name = intent.getStringExtra("shippingName").orEmpty()
        val date = intent.getStringExtra("shippingDate").orEmpty()
        val phone = intent.getStringExtra("shippingPhone").orEmpty()
        val postal = intent.getStringExtra("shippingPostal").orEmpty()
        val address = intent.getStringExtra("shippingAddress").orEmpty()

        findViewById<TextView>(R.id.orderDateText).text =
            "배송일 : $date"

        findViewById<TextView>(R.id.orderShippingText).text =
            "성명 : $name ($phone)\n" +
                    "우편번호 : $postal\n" +
                    "주소 : $address"

        val orderItems = CartStore.items
        val table = findViewById<TableLayout>(R.id.orderItemsTable)

        orderItems.forEach { item ->
            val row = TableRow(this).apply {
                layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, dp(8), 0, dp(8))
            }

            row.addView(
                makeCell(
                    "${item.title}\n${item.id}",
                    130,
                    Gravity.START
                )
            )

            row.addView(
                makeCell(
                    money(item.price.toLong()),
                    64,
                    Gravity.END
                )
            )

            row.addView(
                makeCell(
                    item.quantity.toString(),
                    40,
                    Gravity.CENTER
                )
            )

            row.addView(
                makeCell(
                    money(item.subtotal),
                    72,
                    Gravity.END
                )
            )

            table.addView(row)
        }

        val total = orderItems.sumOf { it.subtotal }

        findViewById<TextView>(R.id.orderTotalText).text =
            "${money(total)}원"

        val completeButton = findViewById<Button>(
            R.id.completeOrderButton
        )

        completeButton.isEnabled = orderItems.isNotEmpty()

        completeButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("도서 주문 완료")
                .setMessage("도서를 주문해 주셔서 감사합니다.")
                .setNegativeButton("아니오", null)
                .setPositiveButton("예") { _, _ ->
                    CartStore.clear()

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
                }
                .show()
        }

        findViewById<Button>(
            R.id.cancelOrderButton
        ).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("주문 취소")
                .setMessage(
                    "주문을 취소하고 장바구니를 비우시겠습니까?"
                )
                .setNegativeButton("아니오", null)
                .setPositiveButton("예") { _, _ ->
                    CartStore.clear()

                    startActivity(
                        Intent(
                            this,
                            CartActivity::class.java
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                    )

                    finish()
                }
                .show()
        }
    }

    private fun makeCell(
        value: String,
        width: Int,
        alignment: Int
    ): TextView {
        return TextView(this).apply {
            layoutParams = TableRow.LayoutParams(
                dp(width),
                TableRow.LayoutParams.WRAP_CONTENT
            )
            text = value
            textSize = 11f
            gravity = alignment
            setTextColor(Color.parseColor("#333333"))
            setPadding(dp(3), dp(4), dp(3), dp(4))
        }
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
package com.example.bookmarket

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ShippingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_shipping)

        val root = findViewById<View>(R.id.shippingRoot)

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

        val toolbar = findViewById<Toolbar>(R.id.shippingToolbar)

        toolbar.setNavigationIcon(
            androidx.appcompat.R.drawable.abc_ic_ab_back_material
        )
        toolbar.navigationIcon?.setTint(Color.WHITE)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val nameInput = findViewById<EditText>(
            R.id.shippingNameInput
        )
        val dateInput = findViewById<EditText>(
            R.id.shippingDateInput
        )
        val phoneInput = findViewById<EditText>(
            R.id.shippingPhoneInput
        )
        val postalInput = findViewById<EditText>(
            R.id.shippingPostalInput
        )
        val addressInput = findViewById<EditText>(
            R.id.shippingAddressInput
        )

        val deliveryDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }

        val dateFormat = SimpleDateFormat(
            "yyyy년 MM월 dd일",
            Locale.KOREA
        )

        dateInput.setText(
            dateFormat.format(deliveryDate.time)
        )

        dateInput.setOnClickListener {
            val picker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    deliveryDate.set(
                        year,
                        month,
                        day
                    )

                    dateInput.setText(
                        dateFormat.format(deliveryDate.time)
                    )
                },
                deliveryDate.get(Calendar.YEAR),
                deliveryDate.get(Calendar.MONTH),
                deliveryDate.get(Calendar.DAY_OF_MONTH)
            )

            picker.datePicker.minDate =
                System.currentTimeMillis()

            picker.show()
        }

        findViewById<Button>(
            R.id.cancelShippingButton
        ).setOnClickListener {
            finish()
        }

        findViewById<Button>(
            R.id.registerShippingButton
        ).setOnClickListener {
            val requiredFields = listOf(
                nameInput,
                phoneInput,
                postalInput,
                addressInput
            )

            val emptyField = requiredFields.firstOrNull {
                it.text.toString().isBlank()
            }

            if (emptyField != null) {
                emptyField.error = "필수 입력 항목입니다"
                emptyField.requestFocus()
                return@setOnClickListener
            }

            val phone = phoneInput.text.toString()
                .filter { it.isDigit() }

            if (phone.length !in 9..11) {
                phoneInput.error = "연락처를 확인하세요"
                phoneInput.requestFocus()
                return@setOnClickListener
            }

            val postal = postalInput.text.toString().trim()

            if (!postal.matches(Regex("\\d{5}"))) {
                postalInput.error = "우편번호 5자리를 입력하세요"
                postalInput.requestFocus()
                return@setOnClickListener
            }

            if (CartStore.items.isEmpty()) {
                Toast.makeText(
                    this,
                    "장바구니에 도서를 담아주세요",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val orderIntent = Intent(
                this,
                OrderActivity::class.java
            ).apply {
                putExtra(
                    "shippingName",
                    nameInput.text.toString().trim()
                )
                putExtra(
                    "shippingDate",
                    dateInput.text.toString()
                )
                putExtra("shippingPhone", phone)
                putExtra("shippingPostal", postal)
                putExtra(
                    "shippingAddress",
                    addressInput.text.toString().trim()
                )
            }

            startActivity(orderIntent)
        }
    }
}
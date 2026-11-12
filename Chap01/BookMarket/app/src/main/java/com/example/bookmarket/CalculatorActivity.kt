package com.example.bookmarket

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.math.BigDecimal
import java.math.MathContext

class CalculatorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calculator)

        val root = findViewById<View>(R.id.calculatorRoot)
        val spacing = dp(20)

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
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

        findViewById<Button>(
            R.id.openCalculatorButton
        ).setOnClickListener {
            showCalculator()
        }
    }

    private fun showCalculator() {
        val content = layoutInflater.inflate(
            R.layout.dialog_calculator,
            null
        )

        val expression = content.findViewById<TextView>(
            R.id.calculationExpression
        )
        val result = content.findViewById<TextView>(
            R.id.calculationResult
        )
        val keys = content.findViewById<GridLayout>(
            R.id.calculatorKeys
        )

        val dialog = AlertDialog.Builder(this)
            .setView(content)
            .create()

        var input = "0"
        var storedValue: BigDecimal? = null
        var operation: String? = null
        var startNewNumber = true
        var hasError = false

        fun format(value: BigDecimal): String {
            return value.stripTrailingZeros().toPlainString()
        }

        fun calculate(
            left: BigDecimal,
            right: BigDecimal,
            operator: String
        ): BigDecimal {
            return when (operator) {
                "+" -> left.add(right)
                "-" -> left.subtract(right)
                "*" -> left.multiply(right)
                "/" -> left.divide(right, MathContext.DECIMAL64)
                else -> right
            }
        }

        fun reset() {
            input = "0"
            storedValue = null
            operation = null
            startNewNumber = true
            hasError = false
            expression.text = ""
            result.text = "0"
        }

        fun showError() {
            input = "0"
            storedValue = null
            operation = null
            startNewNumber = true
            hasError = true
            expression.text = "계산할 수 없습니다"
            result.text = "오류"
        }

        fun updateInput() {
            result.text = input

            val left = storedValue
            val operator = operation

            expression.text = if (left != null && operator != null) {
                "${format(left)} $operator $input"
            } else {
                ""
            }
        }

        fun pressKey(key: String) {
            if (key == "AC") {
                reset()
                return
            }

            if (hasError) {
                reset()
            }

            when (key) {
                "C" -> {
                    input = "0"
                    startNewNumber = true
                    updateInput()
                }

                "←" -> {
                    if (!startNewNumber) {
                        input = input.dropLast(1)

                        if (input.isEmpty() || input == "-") {
                            input = "0"
                        }

                        updateInput()
                    }
                }

                "." -> {
                    if (startNewNumber) {
                        input = "0."
                        startNewNumber = false
                    } else if (!input.contains(".")) {
                        input += "."
                    }

                    updateInput()
                }

                "+", "-", "*", "/" -> {
                    try {
                        val current = BigDecimal(input)
                        val left = storedValue
                        val previousOperator = operation

                        val value = if (
                            left != null &&
                            previousOperator != null &&
                            !startNewNumber
                        ) {
                            calculate(left, current, previousOperator)
                        } else {
                            left ?: current
                        }

                        storedValue = value
                        operation = key
                        input = format(value)
                        startNewNumber = true

                        result.text = input
                        expression.text = "${format(value)} $key"
                    } catch (error: ArithmeticException) {
                        showError()
                    }
                }

                "=" -> {
                    val left = storedValue
                    val operator = operation

                    if (left != null && operator != null) {
                        try {
                            val right = BigDecimal(input)
                            val answer = calculate(
                                left,
                                right,
                                operator
                            )

                            expression.text =
                                "${format(left)} $operator " +
                                        "${format(right)} = ${format(answer)}"

                            input = format(answer)
                            result.text = input
                            storedValue = null
                            operation = null
                            startNewNumber = true
                        } catch (error: ArithmeticException) {
                            showError()
                        }
                    }
                }

                else -> {
                    if (startNewNumber) {
                        input = key
                        startNewNumber = false
                    } else if (input.length < 16) {
                        input = if (input == "0") {
                            key
                        } else {
                            input + key
                        }
                    }

                    updateInput()
                }
            }
        }

        val rows = listOf(
            listOf("AC", "C", "*", "/"),
            listOf("7", "8", "9", "-"),
            listOf("4", "5", "6", "+"),
            listOf("1", "2", "3", "="),
            listOf("←", "0", ".")
        )

        rows.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { columnIndex, label ->
                val button = AppCompatButton(this).apply {
                    text = label
                    textSize = 20f
                    isAllCaps = false
                    minWidth = 0
                    minimumWidth = 0
                    minHeight = 0
                    minimumHeight = 0
                    setPadding(0, 0, 0, 0)
                    setTextColor(Color.parseColor("#263238"))

                    backgroundTintList = ColorStateList.valueOf(
                        Color.parseColor(
                            if (label == "=") {
                                "#F47721"
                            } else {
                                "#E3E5E5"
                            }
                        )
                    )

                    contentDescription = when (label) {
                        "AC" -> "전체 초기화"
                        "C" -> "현재 입력 지우기"
                        "←" -> "한 글자 지우기"
                        "*" -> "곱하기"
                        "/" -> "나누기"
                        "=" -> "계산 결과"
                        else -> label
                    }

                    setOnClickListener {
                        pressKey(label)
                    }
                }

                val rowSpan = if (label == "=") 2 else 1

                val params = GridLayout.LayoutParams(
                    GridLayout.spec(
                        rowIndex,
                        rowSpan,
                        GridLayout.FILL
                    ),
                    GridLayout.spec(
                        columnIndex,
                        1,
                        GridLayout.FILL,
                        1f
                    )
                ).apply {
                    width = 0
                    height = dp(if (label == "=") 100 else 48)
                    setMargins(dp(3), dp(2), dp(3), dp(2))
                }

                keys.addView(button, params)
            }
        }

        content.findViewById<Button>(
            R.id.closeCalculatorButton
        ).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        val dialogWidth = minOf(
            dp(340),
            (resources.displayMetrics.widthPixels * 0.9f).toInt()
        )

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                dialogWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
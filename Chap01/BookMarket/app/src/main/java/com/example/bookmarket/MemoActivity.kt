package com.example.bookmarket

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MemoActivity : AppCompatActivity() {

    private lateinit var database: MemoDatabase
    private lateinit var memoList: ListView
    private lateinit var emptyText: TextView
    private lateinit var adapter: BaseAdapter

    private var memos = emptyList<Memo>()
    private var keyword = ""
    private var selectedId: Long? = null

    private val dateFormat = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.KOREA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_memo)

        database = MemoDatabase(this)

        keyword = savedInstanceState
            ?.getString("keyword")
            .orEmpty()

        selectedId = savedInstanceState
            ?.getLong("selectedId", -1L)
            ?.takeIf { it != -1L }

        val root = findViewById<View>(R.id.memoRoot)

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

        val toolbar = findViewById<Toolbar>(R.id.memoToolbar)

        toolbar.setNavigationIcon(
            androidx.appcompat.R.drawable.abc_ic_ab_back_material
        )
        toolbar.navigationIcon?.setTint(Color.WHITE)

        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        memoList = findViewById(R.id.memoList)
        emptyText = findViewById(R.id.memoEmptyText)

        adapter = object : BaseAdapter() {
            override fun getCount(): Int {
                return memos.size
            }

            override fun getItem(position: Int): Memo {
                return memos[position]
            }

            override fun getItemId(position: Int): Long {
                return memos[position].id
            }

            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup?
            ): View {
                return createMemoRow(memos[position])
            }
        }

        memoList.adapter = adapter

        memoList.setOnItemClickListener { _, _, position, _ ->
            selectedId = memos[position].id
            adapter.notifyDataSetChanged()
        }

        findViewById<Button>(
            R.id.addMemoButton
        ).setOnClickListener {
            showMemoEditor(null)
        }

        findViewById<Button>(
            R.id.editMemoButton
        ).setOnClickListener {
            val selected = selectedMemo()

            if (selected == null) {
                showMessage("수정할 메모를 선택하세요")
            } else {
                showMemoEditor(selected)
            }
        }

        findViewById<Button>(
            R.id.deleteMemoButton
        ).setOnClickListener {
            val selected = selectedMemo()

            if (selected == null) {
                showMessage("삭제할 메모를 선택하세요")
            } else {
                AlertDialog.Builder(this)
                    .setTitle("메모장 삭제")
                    .setMessage("선택한 메모를 삭제하겠습니까?")
                    .setNegativeButton("아니오", null)
                    .setPositiveButton("예") { _, _ ->
                        database.delete(selected.id)
                        selectedId = null
                        refreshMemos()
                        showMessage("삭제했습니다")
                    }
                    .show()
            }
        }

        refreshMemos()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val searchView = SearchView(this).apply {
            queryHint = "메모 검색"
            maxWidth = Int.MAX_VALUE
        }

        val searchItem = menu.add(
            Menu.NONE,
            301,
            0,
            "검색"
        ).apply {
            setIcon(R.drawable.icon_search)

            setShowAsAction(
                MenuItem.SHOW_AS_ACTION_ALWAYS or
                        MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW
            )

            actionView = searchView
        }

        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    keyword = query.orEmpty()
                    refreshMemos()
                    searchView.clearFocus()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    keyword = newText.orEmpty()
                    refreshMemos()
                    return true
                }
            }
        )

        searchItem.setOnActionExpandListener(
            object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(
                    item: MenuItem
                ): Boolean {
                    return true
                }

                override fun onMenuItemActionCollapse(
                    item: MenuItem
                ): Boolean {
                    keyword = ""
                    searchView.setQuery("", false)
                    refreshMemos()
                    return true
                }
            }
        )

        if (keyword.isNotEmpty()) {
            val restoredKeyword = keyword
            searchItem.expandActionView()
            searchView.setQuery(restoredKeyword, false)
            searchView.clearFocus()
        }

        return true
    }

    private fun refreshMemos() {
        memos = database.search(keyword)

        if (memos.none { it.id == selectedId }) {
            selectedId = if (keyword.isNotBlank()) {
                memos.firstOrNull()?.id
            } else {
                null
            }
        }

        emptyText.text = if (keyword.isBlank()) {
            "등록된 메모가 없습니다"
        } else {
            "검색 결과가 없습니다"
        }

        emptyText.visibility = if (memos.isEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }

        adapter.notifyDataSetChanged()
    }

    private fun createMemoRow(memo: Memo): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            layoutParams = android.widget.AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            setPadding(dp(10), dp(12), dp(10), dp(12))

            setBackgroundColor(
                Color.parseColor(
                    if (memo.id == selectedId) {
                        "#F8CE94"
                    } else {
                        "#FFFFFF"
                    }
                )
            )
        }

        val heading = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        heading.addView(
            TextView(this).apply {
                text = memo.title
                textSize = 15f
                setTextColor(Color.parseColor("#333333"))
                setTypeface(null, android.graphics.Typeface.BOLD)
            },
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        heading.addView(
            TextView(this).apply {
                text = dateFormat.format(Date(memo.updatedAt))
                textSize = 11f
                setTextColor(Color.parseColor("#429DB6"))
                setPadding(dp(8), 0, 0, 0)
            },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        row.addView(heading)

        row.addView(
            TextView(this).apply {
                text = memo.content
                textSize = 13f
                setTextColor(Color.parseColor("#555555"))
                setPadding(0, dp(8), 0, 0)
            },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        return row
    }

    private fun showMemoEditor(memo: Memo?) {
        val editor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(8))
        }

        val titleInput = EditText(this).apply {
            hint = "제목"
            inputType = InputType.TYPE_CLASS_TEXT
            isSingleLine = true
            setText(memo?.title.orEmpty())
            setTextColor(Color.parseColor("#333333"))
            setBackgroundColor(Color.parseColor("#F4AA59"))
            setPadding(dp(10), dp(8), dp(10), dp(8))
        }

        val contentInput = EditText(this).apply {
            hint = "내용"
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE
            gravity = Gravity.TOP or Gravity.START
            setText(memo?.content.orEmpty())
            setTextColor(Color.parseColor("#333333"))
            setBackgroundColor(Color.parseColor("#F8BC7B"))
            setPadding(dp(10), dp(10), dp(10), dp(10))
        }

        editor.addView(
            titleInput,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        editor.addView(
            contentInput,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(180)
            ).apply {
                topMargin = dp(4)
            }
        )

        val dialog = AlertDialog.Builder(this)
            .setTitle("나의 메모장")
            .setView(editor)
            .setNegativeButton("취소", null)
            .setPositiveButton(
                if (memo == null) "등록" else "수정",
                null
            )
            .create()

        dialog.setOnShowListener {
            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener {
                val title = titleInput.text.toString().trim()
                val content = contentInput.text.toString().trim()

                if (title.isEmpty()) {
                    titleInput.error = "제목을 입력하세요"
                    titleInput.requestFocus()
                    return@setOnClickListener
                }

                if (content.isEmpty()) {
                    contentInput.error = "내용을 입력하세요"
                    contentInput.requestFocus()
                    return@setOnClickListener
                }

                selectedId = if (memo == null) {
                    database.insert(title, content)
                } else {
                    database.update(memo.id, title, content)
                    memo.id
                }

                refreshMemos()
                dialog.dismiss()
                showMessage("저장했습니다")
            }
        }

        dialog.show()
    }

    private fun selectedMemo(): Memo? {
        return memos.find { it.id == selectedId }
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("keyword", keyword)
        outState.putLong("selectedId", selectedId ?: -1L)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        database.close()
        super.onDestroy()
    }
}
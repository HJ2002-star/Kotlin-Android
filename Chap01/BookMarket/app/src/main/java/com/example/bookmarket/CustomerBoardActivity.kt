package com.example.bookmarket

import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject

class CustomerBoardActivity : AppCompatActivity() {
    private lateinit var root: LinearLayout
    private lateinit var toolbar: Toolbar
    private lateinit var content: LinearLayout
    private lateinit var actions: LinearLayout
    private var mode = "list"
    private var postId = 0
    private var generation = 0
    private var titleInput: EditText? = null
    private var authorInput: EditText? = null
    private var bodyInput: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }
        toolbar = Toolbar(this).apply {
            setBackgroundColor(Color.rgb(69, 159, 160))
            setTitleTextColor(Color.WHITE)
            setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            navigationIcon?.setTint(Color.WHITE)
            navigationContentDescription = "이전 화면"
            setNavigationOnClickListener { goBack() }
        }
        root.addView(toolbar, LinearLayout.LayoutParams(-1, dp(56)))
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }
        root.addView(content, LinearLayout.LayoutParams(-1, 0, 1f))
        actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(12), dp(4), dp(12), dp(8))
        }
        root.addView(actions, LinearLayout.LayoutParams(-1, -2))
        setContentView(root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = goBack()
        })
        when (savedInstanceState?.getString("mode")) {
            "detail" -> showDetail(savedInstanceState.getInt("postId"))
            "write" -> {
                showWrite()
                titleInput?.setText(savedInstanceState.getString("draftTitle"))
                authorInput?.setText(savedInstanceState.getString("draftAuthor"))
                bodyInput?.setText(savedInstanceState.getString("draftBody"))
            }
            else -> showList()
        }
    }

    private fun reset(nextMode: String, title: String): Int {
        generation++
        mode = nextMode
        toolbar.title = title
        content.removeAllViews()
        actions.removeAllViews()
        titleInput = null
        authorInput = null
        bodyInput = null
        return generation
    }

    private fun showList() {
        val request = reset("list", "고객문의")
        val status = label("게시글을 불러오는 중입니다", 14f)
        content.addView(status)
        action("글쓰기") { showWrite() }
        BoardApi.request("/posts") { result ->
            if (!isCurrent(request)) return@request
            result.onSuccess { json ->
                content.removeAllViews()
                val posts = json.getJSONArray("posts")
                if (posts.length() == 0) {
                    content.addView(label("등록된 문의가 없습니다. 글쓰기로 문의를 남겨 주세요.", 14f))
                } else {
                    val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
                    val scroll = ScrollView(this).apply { addView(list) }
                    content.addView(scroll, LinearLayout.LayoutParams(-1, -1))
                    for (index in 0 until posts.length()) {
                        val post = posts.getJSONObject(index)
                        val row = LinearLayout(this).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(dp(4), dp(12), dp(4), dp(12))
                            contentDescription = "${post.getString("title")} 상세보기"
                            setOnClickListener { showDetail(post.getInt("id")) }
                        }
                        row.addView(label(post.getString("title"), 16f))
                        row.addView(label("${post.getString("author")}    ${post.getString("created_at")}", 11f).apply {
                            setTextColor(Color.rgb(67, 154, 184))
                        })
                        list.addView(row)
                        list.addView(View(this).apply { setBackgroundColor(Color.rgb(230, 230, 230)) }, LinearLayout.LayoutParams(-1, dp(1)))
                    }
                }
            }.onFailure {
                status.text = "서버에 연결하지 못했습니다. PC의 게시판 서버 실행 상태를 확인해 주세요."
                action("다시 시도") { showList() }
            }
        }
    }

    private fun showDetail(identifier: Int) {
        postId = identifier
        val request = reset("detail", "상세보기")
        val status = label("게시글을 불러오는 중입니다", 14f)
        content.addView(status)
        action("닫기") { showList() }
        BoardApi.request("/posts/$identifier") { result ->
            if (!isCurrent(request)) return@request
            result.onSuccess { post ->
                content.removeAllViews()
                field("제목", post.getString("title"))
                field("작성일", post.getString("created_at"))
                field("작성자", post.getString("author"))
                content.addView(label("내용", 13f).apply { setPadding(0, dp(12), 0, dp(8)) })
                val body = label(post.getString("content"), 15f).apply {
                    setTextIsSelectable(true)
                    setPadding(dp(12), dp(12), dp(12), dp(12))
                }
                content.addView(ScrollView(this).apply {
                    setBackgroundColor(Color.rgb(239, 235, 240))
                    isFillViewport = true
                    addView(body, android.widget.FrameLayout.LayoutParams(-1, -2))
                }, LinearLayout.LayoutParams(-1, 0, 1f))
            }.onFailure {
                status.text = "게시글을 불러오지 못했습니다."
                action("다시 시도") { showDetail(identifier) }
            }
        }
    }

    private fun showWrite() {
        reset("write", "글쓰기")
        titleInput = input("제목", 100).also { content.addView(it) }
        authorInput = input("작성자", 30).also { content.addView(it) }
        content.addView(label("내용", 13f).apply { setPadding(0, dp(10), 0, dp(8)) })
        bodyInput = input("문의 내용을 입력하세요", 5000).apply {
            gravity = Gravity.TOP or Gravity.START
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setSingleLine(false)
        }.also { content.addView(it, LinearLayout.LayoutParams(-1, 0, 1f)) }
        action("등록하기") { submit() }
        action("취소하기") { showList() }
    }

    private fun submit() {
        val title = titleInput?.text.toString().trim()
        val author = authorInput?.text.toString().trim()
        val body = bodyInput?.text.toString().trim()
        if (title.isBlank() || author.isBlank() || body.isBlank()) {
            Toast.makeText(this, "제목, 작성자, 내용을 모두 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }
        val request = generation
        for (index in 0 until actions.childCount) actions.getChildAt(index).isEnabled = false
        val data = JSONObject().put("title", title).put("author", author).put("content", body)
        BoardApi.request("/posts", data) { result ->
            if (!isCurrent(request)) return@request
            for (index in 0 until actions.childCount) actions.getChildAt(index).isEnabled = true
            result.onSuccess {
                Toast.makeText(this, "서버에 등록했습니다", Toast.LENGTH_SHORT).show()
                showList()
            }.onFailure {
                Toast.makeText(this, "등록하지 못했습니다. 서버 연결을 확인해 주세요.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun field(name: String, value: String) {
        val row = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        row.addView(label(name, 12f), LinearLayout.LayoutParams(dp(60), -2))
        row.addView(label(value, 13f).apply {
            setPadding(dp(8), dp(10), dp(8), dp(10))
            setBackgroundColor(Color.rgb(239, 235, 240))
            setTextIsSelectable(true)
        }, LinearLayout.LayoutParams(0, -2, 1f))
        content.addView(row, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(6) })
    }

    private fun label(value: String, size: Float) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(Color.rgb(55, 55, 55))
    }

    private fun input(hintText: String, length: Int) = EditText(this).apply {
        hint = hintText
        textSize = 15f
        setTextColor(Color.rgb(55, 55, 55))
        setHintTextColor(Color.GRAY)
        setSingleLine(true)
        filters = arrayOf(InputFilter.LengthFilter(length))
        setPadding(dp(10), dp(10), dp(10), dp(10))
        setBackgroundColor(Color.rgb(239, 235, 240))
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) }
    }

    private fun action(text: String, callback: () -> Unit) {
        actions.addView(Button(this).apply {
            this.text = text
            textSize = 14f
            isAllCaps = false
            setTextColor(Color.WHITE)
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(241, 151, 57))
            setOnClickListener { callback() }
        }, LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginStart = dp(2); marginEnd = dp(2) })
    }

    private fun isCurrent(request: Int) = !isFinishing && !isDestroyed && request == generation
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun goBack() { if (mode == "list") finish() else showList() }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("mode", mode)
        outState.putInt("postId", postId)
        outState.putString("draftTitle", titleInput?.text?.toString())
        outState.putString("draftAuthor", authorInput?.text?.toString())
        outState.putString("draftBody", bodyInput?.text?.toString())
        super.onSaveInstanceState(outState)
    }
}

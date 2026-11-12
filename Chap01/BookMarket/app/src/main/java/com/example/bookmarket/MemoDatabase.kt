package com.example.bookmarket

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Memo(
    val id: Long,
    val title: String,
    val content: String,
    val updatedAt: Long
)

class MemoDatabase(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    "bookmarket_memos.db",
    null,
    1
) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE memos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                content TEXT NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
    }

    fun insert(title: String, content: String): Long {
        val values = ContentValues().apply {
            put("title", title)
            put("content", content)
            put("updated_at", System.currentTimeMillis())
        }

        return writableDatabase.insertOrThrow(
            "memos",
            null,
            values
        )
    }

    fun update(
        id: Long,
        title: String,
        content: String
    ): Int {
        val values = ContentValues().apply {
            put("title", title)
            put("content", content)
            put("updated_at", System.currentTimeMillis())
        }

        return writableDatabase.update(
            "memos",
            values,
            "id = ?",
            arrayOf(id.toString())
        )
    }

    fun delete(id: Long): Int {
        return writableDatabase.delete(
            "memos",
            "id = ?",
            arrayOf(id.toString())
        )
    }

    fun search(keyword: String = ""): List<Memo> {
        val query = keyword.trim()
        val selection = if (query.isEmpty()) {
            null
        } else {
            "instr(title, ?) > 0 OR instr(content, ?) > 0"
        }

        val arguments = if (query.isEmpty()) {
            null
        } else {
            arrayOf(query, query)
        }

        val memos = mutableListOf<Memo>()

        readableDatabase.query(
            "memos",
            arrayOf("id", "title", "content", "updated_at"),
            selection,
            arguments,
            null,
            null,
            "updated_at DESC, id DESC"
        ).use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow("id")
            val titleColumn = cursor.getColumnIndexOrThrow("title")
            val contentColumn = cursor.getColumnIndexOrThrow("content")
            val dateColumn = cursor.getColumnIndexOrThrow("updated_at")

            while (cursor.moveToNext()) {
                memos.add(
                    Memo(
                        id = cursor.getLong(idColumn),
                        title = cursor.getString(titleColumn),
                        content = cursor.getString(contentColumn),
                        updatedAt = cursor.getLong(dateColumn)
                    )
                )
            }
        }

        return memos
    }
}
import argparse
import json
import sqlite3
from datetime import datetime
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlparse

DATABASE = Path(__file__).with_name("data") / "board.sqlite3"


def connect():
    connection = sqlite3.connect(DATABASE, timeout=10)
    connection.row_factory = sqlite3.Row
    return connection


def initialize():
    DATABASE.parent.mkdir(parents=True, exist_ok=True)
    with connect() as connection:
        connection.execute("CREATE TABLE IF NOT EXISTS posts (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, author TEXT NOT NULL, content TEXT NOT NULL, created_at TEXT NOT NULL)")


class Handler(BaseHTTPRequestHandler):
    def reply(self, status, value):
        body = json.dumps(value, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Cache-Control", "no-store")
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        path = urlparse(self.path).path.rstrip("/")
        if path == "/health":
            self.reply(200, {"status": "ok"})
            return
        with connect() as connection:
            if path == "/posts":
                rows = connection.execute("SELECT id, title, author, created_at FROM posts ORDER BY id DESC").fetchall()
                self.reply(200, {"posts": [dict(row) for row in rows]})
                return
            identifier = path.removeprefix("/posts/")
            if path.startswith("/posts/") and identifier.isdigit():
                row = connection.execute("SELECT * FROM posts WHERE id = ?", (int(identifier),)).fetchone()
                self.reply(200, dict(row)) if row else self.reply(404, {"error": "게시글을 찾을 수 없습니다"})
                return
        self.reply(404, {"error": "존재하지 않는 주소입니다"})

    def do_POST(self):
        if urlparse(self.path).path.rstrip("/") != "/posts":
            self.reply(404, {"error": "존재하지 않는 주소입니다"})
            return
        try:
            length = int(self.headers.get("Content-Length", "0"))
            if length <= 0 or length > 32768:
                self.reply(413, {"error": "입력 내용이 너무 깁니다"})
                return
            value = json.loads(self.rfile.read(length).decode("utf-8"))
            fields = {name: value.get(name) for name in ("title", "author", "content")}
            if any(not isinstance(item, str) or not item.strip() for item in fields.values()):
                self.reply(400, {"error": "제목, 작성자, 내용을 모두 입력하세요"})
                return
            fields = {name: item.strip() for name, item in fields.items()}
            if len(fields["title"]) > 100 or len(fields["author"]) > 30 or len(fields["content"]) > 5000:
                self.reply(400, {"error": "입력 가능한 길이를 초과했습니다"})
                return
            created = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            with connect() as connection:
                cursor = connection.execute("INSERT INTO posts(title,author,content,created_at) VALUES(?,?,?,?)", (fields["title"], fields["author"], fields["content"], created))
                row = connection.execute("SELECT * FROM posts WHERE id=?", (cursor.lastrowid,)).fetchone()
            self.reply(201, dict(row))
        except (ValueError, UnicodeDecodeError, AttributeError):
            self.reply(400, {"error": "올바른 JSON 요청이 아닙니다"})


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", type=int, default=8765)
    options = parser.parse_args()
    initialize()
    print(f"BookMarket board server: http://127.0.0.1:{options.port}", flush=True)
    ThreadingHTTPServer(("127.0.0.1", options.port), Handler).serve_forever()

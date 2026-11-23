# BookMarket 고객문의 서버

Python 3.9 이상에서 외부 패키지 없이 실행합니다.

```powershell
python board_server.py
```

서버는 PC의 127.0.0.1:8765에만 연결됩니다. Android 에뮬레이터의 10.0.2.2:8765가 이 서버를 가리킵니다. 실제 휴대전화 연결은 별도 네트워크 설정이 필요합니다.

앱의 `14 Customer Board` 실행 구성 또는 쇼핑 메뉴의 고객센터에서 목록을 열고, 글쓰기로 등록한 뒤 항목을 누르면 상세 내용을 서버에서 조회합니다.

- `GET /health`: 서버 상태
- `GET /posts`: 목록
- `GET /posts/{id}`: 상세 내용
- `POST /posts`: title, author, content 문자열을 담은 JSON으로 등록

게시글은 `data/board.sqlite3`에 저장되며 서버 재시작 후에도 유지됩니다. 개인 게시글 데이터와 로그는 Git에 포함하지 않습니다. 이 서버는 로컬 과제 실습용입니다.
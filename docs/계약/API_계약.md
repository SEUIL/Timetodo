# API 계약

## 공통

- Base URL: `http://localhost:8085`
- 인증 방식: Spring Security session
- 로그인 성공 후 `JSESSIONID` cookie 기준으로 보호 API를 호출한다.
- 인증이 필요한 API에서 session이 없으면 `401 Unauthorized`를 반환한다.
- 다른 사용자의 리소스에 접근하면 service owner 검증에서 `403 Forbidden`을 반환한다.
- 날짜/시간 형식은 `yyyy-MM-dd'T'HH:mm:ss` 형태의 `LocalDateTime` 문자열을 사용한다.
- validation 실패와 application 예외는 공통 error response 형식을 사용한다.

Error response:

```json
{
  "status": 400,
  "message": "요청 값이 올바르지 않습니다.",
  "errors": ["title: 할 일 제목은 필수입니다."],
  "timestamp": "2026-05-28T17:00:00"
}
```

## 인증

### 회원가입

```http
POST /api/user/signup
Content-Type: application/json
```

Request:

```json
{
  "username": "member",
  "password": "password123",
  "email": "member@example.com"
}
```

Response `200`:

```text
UserEntity registered successfully
```

### 로그인

```http
POST /api/user/login
Content-Type: application/json
```

Request:

```json
{
  "username": "member",
  "password": "password123"
}
```

Response `200`:

```text
Login successful
```

Response `401`:

```text
Invalid username or password
```

### 현재 사용자

```http
GET /api/user/me
```

Response `200`:

```json
{
  "userId": 1
}
```

### 로그아웃

```http
POST /api/user/logout
```

Response `200`:

```text
Logout successful
```

## Category

### 생성

```http
POST /api/categories
Content-Type: application/json
```

Request:

```json
{
  "categoryName": "개발",
  "color": "#4f46e5"
}
```

Response `200`:

```json
{
  "categoryId": 1,
  "categoryName": "개발",
  "color": "#4f46e5"
}
```

### 목록

```http
GET /api/categories
```

Response `200`:

```json
[
  {
    "categoryId": 1,
    "categoryName": "개발",
    "color": "#4f46e5"
  }
]
```

### 수정

```http
PUT /api/categories/{categoryId}
Content-Type: application/json
```

Request:

```json
{
  "categoryName": "운영",
  "color": "#16a34a"
}
```

### 삭제

```http
DELETE /api/categories/{categoryId}
```

Response `204`: body 없음

### 이름 검색

```http
GET /api/categories/search?categoryName=개발
```

Legacy path:

- `POST /api/categories/add`
- `GET /api/categories/all`
- `PUT /api/categories/update/{categoryId}`
- `DELETE /api/categories/delete/{categoryId}`

## Task

### 생성

```http
POST /api/tasks
Content-Type: application/json
```

Request:

```json
{
  "title": "리팩터링 정리",
  "dueDate": "2026-05-28T18:00:00",
  "priority": "일반",
  "status": "보류 중",
  "repeatType": "반복 없음",
  "categoryId": 1
}
```

Response `200`:

```json
{
  "taskId": 1,
  "title": "리팩터링 정리",
  "dueDate": "2026-05-28T18:00:00",
  "priority": "일반",
  "status": "보류 중",
  "repeatType": "반복 없음",
  "userId": 1,
  "categoryId": 1,
  "reminderIds": []
}
```

### 목록

```http
GET /api/tasks
```

### 수정

```http
PUT /api/tasks/{taskId}
Content-Type: application/json
```

Request body는 생성과 동일하며, `taskId`는 path variable을 우선 사용한다.

### 상태 변경

```http
PATCH /api/tasks/{taskId}/status
Content-Type: application/json
```

Request:

```json
{
  "status": "완료"
}
```

Response `200`: body 없음

### 삭제

```http
DELETE /api/tasks/{taskId}
```

Response `204`: body 없음

## Calendar

### 생성

```http
POST /api/calendars
Content-Type: application/json
```

Request:

```json
{
  "title": "회의",
  "description": "프로젝트 진행 상황 공유",
  "startTime": "2026-05-28T10:00:00",
  "end_time": "2026-05-28T11:00:00",
  "location": "온라인",
  "repeatType": "반복 없음",
  "categoryId": 1
}
```

Response `200`:

```json
{
  "calendarId": 1,
  "title": "회의",
  "description": "프로젝트 진행 상황 공유",
  "startTime": "2026-05-28T10:00:00",
  "end_time": "2026-05-28T11:00:00",
  "location": "온라인",
  "repeatType": "반복 없음",
  "userId": 1,
  "categoryId": 1,
  "reminderIds": []
}
```

### 목록

```http
GET /api/calendars
```

### 수정

```http
PUT /api/calendars/{calendarId}
Content-Type: application/json
```

Request body는 생성과 동일하며, `calendarId`는 path variable을 우선 사용한다.

### 삭제

```http
DELETE /api/calendars/{calendarId}
```

Response `204`: body 없음

### 검색

```http
GET /api/calendars/search?title=회의&description=공유&categoryId=1&startTime=2026-05-28T10:00:00
```

Response `200`:

```json
[
  {
    "calendarId": 1,
    "title": "회의",
    "description": "프로젝트 진행 상황 공유",
    "startTime": "2026-05-28T10:00:00",
    "end_time": "2026-05-28T11:00:00",
    "location": "온라인",
    "repeatType": "반복 없음",
    "userId": 1,
    "categoryId": 1,
    "reminderIds": []
  }
]
```

## AI

### 자연어 입력 처리

```http
POST /api/chat/add
Content-Type: text/plain
```

Request:

```text
내일 오후 3시에 회의 일정 추가해줘
```

Response `200`:

```text
요청이 성공적으로 처리되었습니다.
```

주의:

- OpenAI API key가 없거나 호출이 실패하면 `OpenAIAPI_SelectProcessing 실행 실패` 또는 처리 실패 메시지가 반환될 수 있다.
- AI가 생성하는 Task/Calendar는 사용자별 `AI 기본-{userId}` category를 조회하거나 자동 생성해서 사용한다.

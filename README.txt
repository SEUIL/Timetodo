TimeTodo
========

학생 시절 제작한 일정/할 일 관리 프로젝트를 다시 실행 가능한 상태로 복구하고, 구조와 보안 리스크를 단계적으로 개선하는 저장소입니다.

## 프로젝트 개요

TimeTodo는 일정, 할 일, 카테고리, 통계, AI 입력 기반 일정/할 일 생성 기능을 포함한 웹 애플리케이션입니다.

| 영역 | 기술 |
|---|---|
| Frontend | React 18, Create React App, axios, FullCalendar |
| Backend | Java 17, Spring Boot 3.3.4, Spring MVC, Spring Data JPA |
| DB | MySQL |
| AI | OpenAI API 연동 |

## 현재 구조

```text
Timetodo/
  README.txt
  frontEnd/
  backEnd/
  docs/
```

## 주요 개선 이력

### 1. 프로젝트 구조 정리

기존에는 프론트엔드와 백엔드가 `TimeTODOhh/` 한 폴더 안에 섞여 있었다.

현재는 아래처럼 분리했다.

- `frontEnd/`: React 앱
- `backEnd/`: Spring Boot 앱
- `docs/`: 진단, 개선 기록, 계획 문서

### 2. 환경변수 분리

DB 계정, DB 비밀번호, OpenAI API key 등 유출되면 안 되는 값은 `backEnd/.env`로 분리했다.

Git에는 실제 `.env`를 올리지 않고, 필요한 키 목록만 `backEnd/.env.example`에 남긴다.

### 3. 인증/보안 1차 개선

초기 코드에는 다음 문제가 있었다.

- 로그인 실패 시 `NullPointerException`으로 500 발생 가능
- 비밀번호 평문 저장
- credential 허용 CORS에서 전체 origin 허용

개선 내용:

- 로그인 실패 시 `401 Unauthorized` 반환
- BCrypt 기반 비밀번호 hash 저장
- CORS 허용 origin을 `.env` 기반 설정으로 제한
- 쿠키 `HttpOnly`, `SameSite`, `Secure` 설정 추가

### 4. 인증/권한 2차 개선

초기 구조는 `userId` 쿠키 값을 서버가 그대로 신뢰했다. 클라이언트가 쿠키 값을 바꾸면 다른 사용자의 데이터에 접근할 수 있는 구조였다.

개선 내용:

- 로그인 성공 시 서버 `HttpSession`에 사용자 ID 저장
- 보호 API는 세션 기준으로 현재 사용자 확인
- `/api/user/me` 추가
- Task, Calendar, Category, Reminder, Log owner 검증 추가
- Entity 직접 반환을 DTO 반환으로 정리
- 프론트 API 호출을 공통 axios client로 통합

### 5. 유지보수 구조 3차 개선

기존 코드는 Entity 관계 필드명이 실제 타입과 맞지 않고, Task/Calendar API path가 동사 중심으로 흩어져 있었다. AI 기능도 OpenAI 호출, 응답 parsing, DB 생성 로직이 한 service에 집중되어 있었다.

개선 내용:

- Entity 관계 필드명을 `user`, `category`, `reminders`처럼 실제 타입에 맞게 정리
- Task API를 `POST /api/tasks`, `PUT /api/tasks/{taskId}`, `PATCH /api/tasks/{taskId}/status` 형태로 정리
- Calendar API를 `POST /api/calendars`, `PUT /api/calendars/{calendarId}` 형태로 정리
- Spring Security session 기반 인증 구조 도입
- OpenAI HTTP 호출, AI 응답 parsing, command dispatch, 도메인 생성 책임 분리
- Flyway 기반 DB migration 도입
- 프론트 build warning 0개로 정리

## 포트폴리오 관점 정리

이 프로젝트는 “처음부터 완성도 높게 설계한 프로젝트”라기보다, 학생 시절 만든 기능 중심 코드를 실제 유지보수 가능한 구조로 고쳐가는 사례로 보는 편이 적절하다.

핵심 서술 방향:

- 기존 문제: 프론트/백엔드 폴더 혼재, 비밀값 노출 위험, 평문 비밀번호, 클라이언트 `userId` 신뢰, owner 검증 부족, Entity/API naming 혼란
- 개선 접근: 실행 환경 복구 -> 보안 위험 제거 -> 인증/권한 경계 정리 -> API 계약 정리 -> 테스트와 문서화 보강
- 결과: 로컬 실행 가능, DB/AI key 환경변수 분리, BCrypt 적용, Spring Security session 인증 적용, 주요 owner 검증, controller 인증, AI parsing 테스트 추가, Flyway migration 적용

포트폴리오에서는 “이런 문제가 있었습니다”에서 끝내기보다 “문제를 식별했고, 우선순위를 나눠 이렇게 개선했습니다” 흐름으로 설명한다.

## 로컬 실행

자세한 실행 절차는 아래 문서를 기준으로 한다.

- `docs/실행/로컬_실행_가이드.md`

요약:

```bash
cd backEnd
bash ./mvnw spring-boot:run
```

```bash
cd frontEnd
npm run start
```

## 검증 상태

현재 확인된 검증 명령:

```bash
cd backEnd && bash ./mvnw test
cd frontEnd && npm run build
```

두 명령 모두 성공했다. 프론트 build는 ESLint warning 0개 상태다.

## 문서

| 문서 | 설명 |
|---|---|
| `docs/진단/1차_구조_진단.md` | 초기 구조와 리스크 진단 |
| `docs/기록/1차_개선_기록.md` | 비밀번호, 로그인 실패, CORS 개선 기록 |
| `docs/기록/2차_개선_기록.md` | 세션 인증, owner 검증, API client 개선 기록 |
| `docs/기록/3차_개선_기록.md` | Entity/API/Spring Security/AI 1차 개선 기록 |
| `docs/계약/API_계약.md` | 주요 API endpoint, request, response 계약 |
| `docs/계획/남은_작업.md` | 다음 작업 백로그 |
| `docs/계획/2차_개선_계획.md` | 2차 개선 백로그와 우선순위 |
| `docs/계획/코드진단_및_OpenCode_인계_계획.md` | 대규모 리뷰 인계 계획 |

## 남은 주요 작업

- AI 기본 category ID `1` 하드코딩 제거
- Controller 통합 테스트 확대
- 배포 또는 포트폴리오 README 시각 자료 보강

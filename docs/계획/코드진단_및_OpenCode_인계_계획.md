# 1차 구조 진단 및 OpenCode 인계 계획

## 목적

현재 프로젝트는 학생 시절 구현된 Spring Boot 백엔드와 React 프론트엔드가 한 디렉터리에 섞여 있던 상태에서 시작했다. 우선 작업환경을 복구하고 폴더 구조를 분리했으므로, 바로 대규모 리팩터링에 들어가기보다 짧은 1차 진단으로 리스크와 우선순위를 먼저 확정한다.

1차 진단 결과를 바탕으로 OpenCode에 넘길 작업 범위와 입력 자료를 정리하면, 대규모 코드 리뷰나 정리 작업에서 불필요한 탐색 비용을 줄일 수 있다.

## 현재 기준 구조

```text
Timetodo/
  README.txt
  frontEnd/
  backEnd/
  docs/
```

| 영역 | 위치 | 역할 |
|---|---|---|
| 프론트엔드 | `frontEnd/` | Create React App 기반 React 앱 |
| 백엔드 | `backEnd/` | Spring Boot, Maven, JPA, MySQL |
| 문서 | `docs/` | 진단 결과, 인계 계획, 개발 기록 |
| 루트 README | `README.txt` | 기존 위치 유지 |

## 현재 검증된 상태

| 항목 | 상태 |
|---|---|
| 백엔드 build | `cd backEnd && bash ./mvnw -DskipTests package` 성공 |
| 프론트 build | `cd frontEnd && npm run build` 성공, ESLint warning 존재 |
| DB | Docker MySQL 컨테이너의 `timetodo` DB 사용 |
| 백엔드 기동 | `backEnd/.env` 로딩, MySQL 연결, 8085 기동 확인 |
| secret 관리 | 백엔드 DB credential과 OpenAI key는 `.env`로 분리 |

## 1차 진단 범위

1차 진단은 코드를 바로 고치는 작업이 아니라, 구조와 리스크를 지도처럼 정리하는 작업이다. 목표 시간은 약 30분이며, 산출물은 `docs/` 아래 Markdown 문서로 남긴다.

### 1. 실행 구조

- 백엔드 진입점, 포트, env, DB 연결 방식
- 프론트 진입점, proxy 설정, hardcoded API URL
- 로컬 실행 순서와 실패 가능 지점

### 2. 보안 리스크

- secret이 Git tracked 파일에 남아 있는지 확인
- CORS 설정 수준
- 로그인/세션/cookie 처리 방식
- 비밀번호 저장 방식
- OpenAI API 호출 위치와 key 사용 방식

### 3. 백엔드 구조

- Controller, Service, Repository 책임 분리 상태
- Entity 관계와 DB schema 생성 방식
- transaction 처리 여부
- DTO와 Entity 변환 방식
- 예외 처리, validation, status code 처리

### 4. 프론트 구조

- 라우팅 구조
- API 호출 방식 통일 여부
- cookie/session 사용 방식
- 상태 관리 방식
- component 분리와 중복 호출
- hardcoded URL 제거 후보

### 5. 우선순위 분류

진단 결과는 아래 세 그룹으로 나눈다.

| 그룹 | 의미 |
|---|---|
| 즉시 수정 | 실행 실패, secret 노출, 데이터 손상 가능성 |
| 단기 정리 | API 호출 통일, env 정리, README 보강, validation |
| 장기 리팩터링 | 인증 구조, 도메인 모델 재설계, 프론트 구조 개편 |

## OpenCode로 넘길 기준

아래 조건 중 하나라도 해당하면 OpenCode로 넘기는 편이 효율적이다.

- 파일 수가 많고 cross-cutting 변경이 필요한 경우
- 프론트/백엔드/API 계약을 동시에 점검해야 하는 경우
- 테스트 보강, 구조 리팩터링, 문서 정리를 병렬로 진행해야 하는 경우
- 긴 코드 리뷰 리포트와 단계별 수정 계획이 필요한 경우

Codex는 현재 작업환경, 실행 검증, 로컬 변경 맥락을 이미 알고 있으므로 1차 진단에 적합하다. OpenCode는 그 이후 넓은 코드 리뷰와 대규모 정리에 적합하다.

## OpenCode 인계 패키지

OpenCode에 넘기기 전 최소한 아래 자료를 준비한다.

| 자료 | 위치 |
|---|---|
| 현재 실행 방법 | `docs/local-runbook.md` 또는 1차 진단 문서 |
| 1차 구조 진단 결과 | `docs/first-pass-diagnosis.md` |
| 리스크 맵 | `docs/first-pass-diagnosis.md` 내 섹션 |
| 변경 금지/주의 사항 | 이 문서 또는 별도 handoff 문서 |
| 검증 명령 | 백엔드/프론트 build 명령 |

## OpenCode 작업 지시서 초안

```text
Timetodo 프로젝트의 1차 진단 문서를 기준으로 코드 리뷰를 진행해줘.

목표:
- 실행을 깨지 않고 보안, 구조, 유지보수 리스크를 우선순위별로 정리
- 프론트/백엔드/API 계약 불일치 확인
- 바로 수정할 항목과 장기 리팩터링 항목 분리

범위:
- frontEnd/
- backEnd/
- docs/

제외:
- .env 값 노출 금지
- 대규모 리팩터링 즉시 적용 금지
- 기존 동작을 확인하지 않은 기능 삭제 금지

검증 기준:
- cd backEnd && bash ./mvnw -DskipTests package
- cd frontEnd && npm run build
- 필요 시 backEnd Spring Boot 기동 확인

산출물:
- findings 우선순위 목록
- 수정 제안
- 테스트/검증 부족분
- 다음 단계 작업 계획
```

## 다음 단계

1. Codex가 `docs/first-pass-diagnosis.md`를 작성한다.
2. 사용자가 우선순위를 확인한다.
3. 수정 범위가 작으면 Codex에서 바로 처리한다.
4. 범위가 넓으면 이 문서와 1차 진단 문서를 OpenCode에 넘긴다.


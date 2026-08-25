#  프로젝트 · 작업 협업 API 과제

---
# .백엔드 및 프론트 배포 주소
- Swagger UI 주소 (개인 NAS에 docker배포) 
- http://1.234.196.160:8080/swagger-ui/index.html#/


- 프론트화면 react(클라우드 배포)
- test용 id : owner@example.com
- https://collab-fe-liard.vercel.app/



---
## 실행 


### 1. 데이터베이스 생성 
DB 접속 정보는 `application.yaml`에 들어 있고, 환경변수로 덮어쓸 수 있습니다

`.env` 파일을 두면 `spring.config.import`로 자동으로 읽힙니다.
### .env
```bash

POSTGRES_HOST=
POSTGRES_PORT=
POSTGRES_DB=
POSTGRES_USER=
POSTGRES_PASSWORD=
```
```bash
psql -h 1.234.196.160 -p 15432 -U postgres -f db/init.sql
```

### 2. 로컬 실행
```bash
./gradlew bootRun
```


---

## 1. REST API 명세 요약


### 1.1 엔드포인트 (17개)

| 메서드 | 경로 | 설명 | 권한 |
|---|---|---|---|
| `POST` | `/api/users` | 사용자 등록 | 누구나 (헤더 불필요) |
| `GET` | `/api/users?email=` | 이메일로 조회 | 누구나 (헤더 불필요) |
| `GET` | `/api/users/{userId}` | 사용자 조회 | 누구나 |
| `POST` | `/api/projects` | 프로젝트 생성 | 누구나 (생성자가 OWNER) |
| `GET` | `/api/projects` | 내가 속한 프로젝트 목록 | 본인 |
| `GET` | `/api/projects/{projectId}` | 프로젝트 상세 | 멤버 전원 |
| `PATCH` | `/api/projects/{projectId}` | 프로젝트 수정 | OWNER · ADMIN |
| `DELETE` | `/api/projects/{projectId}` | 프로젝트 삭제 | OWNER |
| `GET` | `/api/projects/{projectId}/members` | 멤버 목록 | 멤버 전원 |
| `POST` | `/api/projects/{projectId}/members` | 멤버 추가 | OWNER · ADMIN |
| `PATCH` | `/api/projects/{projectId}/members/{userId}` | 역할 변경 | OWNER · ADMIN |
| `DELETE` | `/api/projects/{projectId}/members/{userId}` | 멤버 제거 | OWNER · ADMIN |
| `POST` | `/api/projects/{projectId}/tasks` | 작업 생성 | 멤버 전원 |
| `GET` | `/api/projects/{projectId}/tasks` | 작업 목록 (검색 · 필터 · 페이징) | 멤버 전원 |
| `GET` | `/api/projects/{projectId}/tasks/{taskId}` | 작업 상세 | 멤버 전원 |
| `PATCH` | `/api/projects/{projectId}/tasks/{taskId}` | 작업 수정 | 담당자 · OWNER · ADMIN |
| `DELETE` | `/api/projects/{projectId}/tasks/{taskId}` | 작업 삭제 | 담당자 · OWNER · ADMIN |

### 1.2 파라미터

작업 목록 조회 — `GET /api/projects/{projectId}/tasks`

| 이름 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| `keyword` | string | | 제목 부분 일치 (대소문자 무시) |
| `status` | string | | `TODO` · `IN_PROGRESS` · `DONE` |
| `assigneeId` | number | | 담당자로 필터 |
| `unassigned` | boolean | `false` | `true`면 담당자 미지정만 |
| `page` | number | `0` | 0부터 시작 |
| `size` | number | `20` | 최대 100 |
| `sort` | string | `createdAt,desc` | `id`가 마지막 정렬 키로 항상 덧붙음 |

조건은 모두 AND로 결합하며, 지정하지 않은 조건은 무시합니다.

작업 생성 — `POST /api/projects/{projectId}/tasks`

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| `title` | string | O | 1~200자 |
| `description` | string | | 2000자 이하 |
| `assigneeId` | number | | 그 프로젝트의 멤버. 생략 · `null`이면 미지정 |
| `status` | string | | 기본값 `TODO` |

작업 수정 — `PATCH /api/projects/{projectId}/tasks/{taskId}`

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `version` | number | O | 조회 때 받은 값. 낙관적 락 검사 |
| `title` | string | | `null` 불가 |
| `description` | string | | `null`이면 비움 |
| `assigneeId` | number | | `null`이면 담당자 해제 |
| `status` | string | | `null` 불가 |

보낸 필드만 바뀝니다. 생략과 명시적 `null`을 구별합니다

### 1.3 응답 예시

프로젝트 생성 — `201 Created`

```http
POST /api/projects        X-User-Id: 3

{ "name": "3분기 개편", "description": "결제 화면 리뉴얼" }
```

```http
HTTP/1.1 201 Created
Location: /api/projects/1
```

```json
{
  "id": 1,
  "name": "3분기 개편",
  "description": "결제 화면 리뉴얼",
  "myRole": "OWNER",
  "memberCount": 1,
  "createdAt": "2026-08-25T14:03:11",
  "updatedAt": "2026-08-25T14:03:11"
}
```

작업 목록 조회 — `200 OK`

```http
GET /api/projects/1/tasks?keyword=결제&status=IN_PROGRESS&page=0&size=20        X-User-Id: 3
```

```json
{
  "content": [
    {
      "id": 7,
      "title": "결제 실패 로그 정리",
      "assignee": { "userId": 8, "name": "이하늘" },
      "status": "IN_PROGRESS",
      "version": 2,
      "createdAt": "2026-08-26T10:14:02",
      "updatedAt": "2026-08-26T11:40:55"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 3,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

동시 수정 충돌 — `409 Conflict`

```json
{
  "code": "TASK_VERSION_CONFLICT",
  "message": "다른 사용자가 먼저 수정했습니다. 최신 내용을 확인해 주세요.",
  "path": "/api/projects/1/tasks/7",
  "timestamp": "2026-08-26T11:41:02.117",
  "currentVersion": 3
}
```

검증 실패 — `400 Bad Request`

모든 오류는 같은 형태로 나가며, 검증 실패일 때만 `errors` 배열이 추가됩니다.

```json
{
  "code": "INVALID_REQUEST",
  "message": "요청 값이 올바르지 않습니다.",
  "path": "/api/projects",
  "timestamp": "2026-08-25T14:03:11.482",
  "errors": [
    { "field": "name", "reason": "프로젝트 이름은 필수입니다." }
  ]
}
```

---

## 2. 주요 설계 결정과 그 이유

### 2.1 역할은 사용자 고정값이 아니라 프로젝트마다 다름

같은 사람이 프로젝트마다 다른 역할을 가지므로 `users.role`로는 표현조차 할 수 없습니다.
`project_members(project_id, user_id, role)`에 두고 `UNIQUE (project_id, user_id)`를 걸어
권한 판정이 항상 단일 행 조회가 되게 했습니다.
`projects.owner_id` 같은 중복 컬럼은 두지 않았습니다 — OWNER가 한 곳에만 있어야 두 정보가 어긋날 일이 없습니다.

### 2.2 비멤버에게는 403이 아니라 404

403을 주면 "그 프로젝트는 존재하지만 당신은 권한이 없다"가 새어 나가, ID를 훑어 존재 여부를 알아낼 수 있습니다.
그래서 프로젝트 밖의 사람에게는 404를 주고, 403은 이미 멤버인 사람이 역할 때문에 거부될 때만 씁니다.

| 상황 | 응답 |
|---|---|
| 없는 프로젝트에 접근 | `404 PROJECT_NOT_FOUND` |
| 멤버가 아닌 사용자가 접근 | `404 PROJECT_NOT_FOUND` (같은 응답 — 존재를 숨김) |
| 다른 프로젝트의 작업 ID를 끼워 넣음 | `404 TASK_NOT_FOUND` |
| MEMBER가 프로젝트를 수정 · 삭제 | `403 PROJECT_FORBIDDEN` |
| ADMIN이 프로젝트를 삭제 (OWNER 전용) | `403 PROJECT_FORBIDDEN` |
| ADMIN이 OWNER 역할을 부여 · 회수 | `403 OWNER_ROLE_FORBIDDEN` |
| 담당자도 OWNER · ADMIN도 아닌 멤버가 작업을 수정 · 삭제 | `403 TASK_FORBIDDEN` |

경계는 하나입니다. 프로젝트 안에 있으면 403, 밖에 있으면 404.
403을 받았다는 것 자체가 이미 그 프로젝트의 멤버라는 뜻이므로 새어 나갈 정보가 없습니다.

같은 이유로 담당자 지정 시 "없는 사용자"와 "멤버가 아닌 사용자"를 구분하지 않고 둘 다 `ASSIGNEE_NOT_MEMBER`로 답합니다.

`tasks`가 `project_id`를 직접 갖기 때문에 모든 조회는 `WHERE project_id = ?`로 시작합니다.
담당자를 거쳐 프로젝트를 찾아가는 경로는 두지 않았습니다 — 격리가 조인 하나에 의존하면 언젠가 새어 나갑니다.

### 2.3 동시 수정 — 낙관적 락

비관적 락을 쓰지 않은 이유: 막아야 할 것은 조회와 수정 사이에 사람이 끼어 있는 구간인데,
행 잠금은 요청 하나의 길이를 넘지 못합니다.
1. 클라이언트가 작업 조회 시 version을 받음  
2. 정 요청에 해당 version을 포함  
3. 서버가 요청 버전과 현재 버전을 명시적으로 비교  
4. 다르면 409 TASK_VERSION_CONFLICT 반환  
5. 같으면 작업 변경 후 flush() 실행  
6. Hibernate가 @Version으로 DB 버전을 다시 검사  
7. 중간에 다른 수정이 발생했다면 낙관적 락 예외 발생  
8. 현재 버전을 재조회하여 409 응답에 포함  
9. 충돌이 없으면 버전이 1 증가한 작업 응답 반환

### 2.4  OWNER — 여기는 비관적 락

"프로젝트마다 OWNER가 최소 하나"는 여러 행에 걸친 조건이라 UNIQUE · CHECK로 표현할 수 없어 서비스에서 막습니다.
문제는 두 OWNER가 서로를 제거할 때입니다 — 각자의 트랜잭션에서 "아직 둘"로 보여 둘 다 통과하고 OWNER가 0이 됩니다.
그래서 프로젝트 행을 `PESSIMISTIC_WRITE`로 잠그고 카운트와 변경을 한 트랜잭션에서 처리합니다.

작업 수정과 달리 여기는 비관적 락이 맞습니다 — 조회와 변경 사이에 사람이 끼어 있지 않고 한 요청 안에서 끝납니다.


### 2.5 패키지 구조 — 클린 아키텍처

도메인별(`user` · `project` · `task`) 계층형 위에 클린 아키텍처를 적용했습니다. `task`가 `project`의 구현에 직접 의존하지 않도록 경계에 포트를 두기 위해서입니다.

포트를 둔 곳은 작업이 프로젝트 멤버십을 확인하는 경계 하나입니다.
`MembershipPort`가 `record Membership(boolean canManageAnyTask)`만 돌려주기 때문에
`task`는 `ProjectRole`이라는 타입 자체를 모릅니다. 역할이 넷으로 늘어도 `task`는 재컴파일 대상이 아니고,
`TaskService` 테스트에 `project` 패키지가 필요 없습니다.

반대 방향(멤버 제거 시 담당자 해제)은 포트를 더 두지 않고 도메인 이벤트로 끊었습니다 —
`project`는 이벤트를 발행할 뿐 누가 듣는지 모릅니다. 평범한 `@EventListener`라 같은 트랜잭션에서 함께 커밋됩니다.
규칙은 클래스가 아니라 패키지 단위입니다: `task.external`만 `project`를 import할 수 있습니다.
`user`는 예외로 직접 참조를 허용합니다 — 다른 도메인을 import하지 않아 순환이 생길 수 없고, 끊을 매듭이 없기 때문입니다.


---

## 3. 사용한 기술과 선택 이유

### 3.1 스택

| 항목          | 선택                                    | 이유                                                                            |
|-------------|---------------------------------------|-------------------------------------------------------------------------------|
| 언어 · 프레임워크  | Java 17 · Spring Boot 3.3.13          |                                                                               |
| 빌드          | Gradle 8.14.5                         |                                                                               |
| 영속성         | JPA (Hibernate)                       | `@Version` 낙관적 락                                                              |
| DB          | PostgreSQL (테스트는 H2)                  | 과제 명세는 H2 인메모리를 지정했지만, `데이터 영속성`과 실제 운영에 가까운 환경에서 확인하려고 외부 접속이 열린 DB를 사용하였습니다 |
| 배포 파이프라인 구축(CICD) | Docker · GitHub Actions · GHCR  | main브랜치에 push하면 깃허브Actions가 테스트 -> 이미지 push -> docker에 `배포`까지 처리합니다.          |


---

## 4. 회사 단위로 데이터를 분리해야 한다면



- `companies` 테이블을 만들고 `users` · `projects`에 `company_id`를 추가합니다.
- `tasks` · `project_members`는 부모를 통해 회사가 정해지지만, 조회마다 조인하지 않도록
  `company_id`를 비정규화해 함께 둡니다. 격리 조건은 조인 없이 걸려야 안전합니다.
- `users.email`의 UNIQUE를 `(company_id, email)` 복합으로 바꿉니다.
  회사가 다르면 같은 이메일이 존재할 수 있어야 하고, 이걸 놓치면 다른 회사 사람이
  이미 쓴 이메일 때문에 가입이 막히면서 그 회사에 그 사람이 있다는 사실이 새어 나갑니다.
- 모든 복합 인덱스의 맨 앞에 `company_id`를 둡니다.

- 지금은 `X-User-Id` 헤더 하나로 요청자를 식별합니다. 회사가 생기면 그 사용자가 어느 회사 소속인지가
모든 쿼리의 전제가 되므로, 회사명도 헤더에 넣습니다


---

## 5. 구현하지 못한 부분과 다음 단계

- 작업 목록의 N+1 | 목록 항목마다 담당자를 지연 로딩해 페이지 크기만큼 추가 쿼리가 나갑니다. `@EntityGraph`나 fetch join으로 한 번에 읽어야 합니다 |
- 프로젝트 목록의 N+1 | 프로젝트마다 멤버 수를 세는 쿼리가 따로 나갑니다.  

- 사용자 수정 · 삭제, 프로젝트 탈퇴(본인스스로 탈퇴)같은 기능을 추가 개발하면 좋을 것 같습니다
- 현재 인증이 없어 요청자를 헤더로 받습니다. 토큰 인증을 붙이면 좋을 것 같습니다
- 모니터링을 추가하면 좋을 것 같습니다
- 시간이 더 있었다면 테스트를 꼼꼼히 만들어서 해봤으면 더 좋았을 것 같습니다   

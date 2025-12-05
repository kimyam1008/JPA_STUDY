# API 사용 가이드

JPA JWT 프로젝트의 REST API 상세 가이드입니다.

## 📋 목차

1. [인증 플로우](#인증-플로우)
2. [공통 사항](#공통-사항)
3. [인증 API](#인증-api)
4. [사용자 API](#사용자-api)
5. [에러 응답](#에러-응답)
6. [테스트 방법](#테스트-방법)

---

## 인증 플로우

```
┌─────────┐                                           ┌─────────┐
│ Client  │                                           │ Server  │
└────┬────┘                                           └────┬────┘
     │                                                      │
     │ 1. POST /api/auth/signup                            │
     │ {username, password, email}                         │
     ├────────────────────────────────────────────────────>│
     │                                                      │
     │ 2. 200 OK                                           │
     │ {"message": "회원가입이 완료되었습니다."}              │
     │<────────────────────────────────────────────────────┤
     │                                                      │
     │ 3. POST /api/auth/login                             │
     │ {username, password}                                │
     ├────────────────────────────────────────────────────>│
     │                                                      │
     │ 4. 200 OK                                           │
     │ {accessToken, refreshToken, tokenType}              │
     │<────────────────────────────────────────────────────┤
     │                                                      │
     │ 5. GET /api/user/me                                 │
     │ Authorization: Bearer <accessToken>                 │
     ├────────────────────────────────────────────────────>│
     │                                                      │
     │ 6. 200 OK                                           │
     │ {id, username, email, role}                         │
     │<────────────────────────────────────────────────────┤
     │                                                      │
     │ 7. (Access Token 만료 시)                           │
     │ POST /api/auth/refresh                              │
     │ Refresh-Token: <refreshToken>                       │
     ├────────────────────────────────────────────────────>│
     │                                                      │
     │ 8. 200 OK                                           │
     │ {accessToken, refreshToken, tokenType}              │
     │<────────────────────────────────────────────────────┤
```

---

## 공통 사항

### Base URL
```
http://localhost:8080
```

### Content-Type
모든 요청과 응답은 `application/json` 형식입니다.

### 인증 헤더
인증이 필요한 API는 다음 헤더를 포함해야 합니다:

```
Authorization: Bearer {accessToken}
```

### 토큰 유효기간
- **Access Token**: 1시간 (3,600,000ms)
- **Refresh Token**: 7일 (604,800,000ms)

---

## 인증 API

인증이 필요하지 않은 API입니다.

### 1. 회원가입

새로운 사용자를 등록합니다.

**Endpoint**
```
POST /api/auth/signup
```

**Request Body**
```json
{
  "username": "testuser",
  "password": "password123",
  "email": "test@example.com"
}
```

**Request Fields**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| username | String | ✅ | 사용자명 (고유값) |
| password | String | ✅ | 비밀번호 (BCrypt 암호화됨) |
| email | String | ✅ | 이메일 (고유값) |

**Success Response (200 OK)**
```json
{
  "message": "회원가입이 완료되었습니다."
}
```

**Error Responses**

| Status | Description |
|--------|-------------|
| 400 Bad Request | 이미 존재하는 사용자명 또는 이메일 |

**curl 예시**
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com"
  }'
```

---

### 2. 로그인

사용자 인증 후 JWT 토큰을 발급받습니다.

**Endpoint**
```
POST /api/auth/login
```

**Request Body**
```json
{
  "username": "testuser",
  "password": "password123"
}
```

**Request Fields**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| username | String | ✅ | 사용자명 |
| password | String | ✅ | 비밀번호 |

**Success Response (200 OK)**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsInJvbGUiOiJST0xFX1VTRVIiLCJpYXQiOjE3MDEyMzQ1NjcsImV4cCI6MTcwMTIzODE2N30.abc123...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTcwMTIzNDU2NywiZXhwIjoxNzAxODM5MzY3fQ.def456...",
  "tokenType": "Bearer"
}
```

**Response Fields**

| Field | Type | Description |
|-------|------|-------------|
| accessToken | String | API 인증용 토큰 (유효기간: 1시간) |
| refreshToken | String | 토큰 갱신용 (유효기간: 7일) |
| tokenType | String | 토큰 타입 (항상 "Bearer") |

**Error Responses**

| Status | Description |
|--------|-------------|
| 401 Unauthorized | 잘못된 사용자명 또는 비밀번호 |

**curl 예시**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

---

### 3. 토큰 갱신

Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.

**Endpoint**
```
POST /api/auth/refresh
```

**Request Headers**
```
Refresh-Token: {refreshToken}
```

**Success Response (200 OK)**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer"
}
```

**Error Responses**

| Status | Description |
|--------|-------------|
| 401 Unauthorized | 유효하지 않거나 만료된 Refresh Token |

**curl 예시**
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Refresh-Token: YOUR_REFRESH_TOKEN_HERE"
```

---

### 4. 로그아웃

Refresh Token을 삭제하여 로그아웃합니다.

**Endpoint**
```
POST /api/auth/logout?username={username}
```

**Query Parameters**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| username | String | ✅ | 로그아웃할 사용자명 |

**Success Response (200 OK)**
```json
{
  "message": "로그아웃되었습니다."
}
```

**curl 예시**
```bash
curl -X POST "http://localhost:8080/api/auth/logout?username=testuser"
```

---

## 사용자 API

인증이 필요한 API입니다. `Authorization` 헤더에 Access Token을 포함해야 합니다.

### 1. 내 정보 조회

현재 로그인한 사용자의 정보를 조회합니다.

**Endpoint**
```
GET /api/user/me
```

**Request Headers**
```
Authorization: Bearer {accessToken}
```

**Success Response (200 OK)**
```json
{
  "id": 1,
  "username": "testuser",
  "email": "test@example.com",
  "role": "ROLE_USER"
}
```

**Response Fields**

| Field | Type | Description |
|-------|------|-------------|
| id | Long | 사용자 ID |
| username | String | 사용자명 |
| email | String | 이메일 |
| role | String | 권한 (ROLE_USER 또는 ROLE_ADMIN) |

**Error Responses**

| Status | Description |
|--------|-------------|
| 401 Unauthorized | 토큰이 없거나 유효하지 않음 |
| 403 Forbidden | 접근 권한 없음 |

**curl 예시**
```bash
curl -X GET http://localhost:8080/api/user/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

---

### 2. 관리자 전용 엔드포인트

관리자 권한이 필요한 테스트용 엔드포인트입니다.

**Endpoint**
```
GET /api/user/admin
```

**Request Headers**
```
Authorization: Bearer {accessToken}
```

**Required Role**
```
ROLE_ADMIN
```

**Success Response (200 OK)**
```json
"관리자만 접근 가능한 엔드포인트입니다."
```

**Error Responses**

| Status | Description |
|--------|-------------|
| 401 Unauthorized | 토큰이 없거나 유효하지 않음 |
| 403 Forbidden | ROLE_ADMIN 권한 없음 |

**curl 예시**
```bash
curl -X GET http://localhost:8080/api/user/admin \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

---

## 에러 응답

### 공통 에러 형식

```json
{
  "timestamp": "2024-12-05T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "이미 존재하는 사용자명입니다.",
  "path": "/api/auth/signup"
}
```

### 주요 에러 코드

| Status Code | Description | 발생 상황 |
|-------------|-------------|-----------|
| 400 Bad Request | 잘못된 요청 | 중복된 사용자명/이메일 |
| 401 Unauthorized | 인증 실패 | 잘못된 비밀번호, 유효하지 않은 토큰 |
| 403 Forbidden | 권한 없음 | 관리자 권한 필요 |
| 404 Not Found | 리소스 없음 | 존재하지 않는 사용자 |
| 500 Internal Server Error | 서버 오류 | 예상치 못한 오류 |

---

## 테스트 방법

### 방법 1: IntelliJ HTTP Client (추천)

프로젝트 루트의 `api-test.http` 파일을 사용하세요.

**사용 순서:**
1. IntelliJ에서 `api-test.http` 파일 열기
2. 회원가입 요청 실행 (▶️ 버튼 클릭)
3. 로그인 요청 실행
4. 응답에서 `accessToken` 복사
5. "내 정보 조회" 요청의 `YOUR_ACCESS_TOKEN_HERE`를 복사한 토큰으로 교체
6. 요청 실행

**장점:**
- GUI 환경에서 편리하게 테스트
- 응답 결과를 이쁘게 포맷팅
- 히스토리 자동 저장

---

### 방법 2: curl (터미널)

**전체 플로우 예시:**

```bash
# 1. 회원가입
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123","email":"test@example.com"}'

# 2. 로그인 (토큰 발급)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'

# 응답 예시:
# {"accessToken":"eyJhbG...","refreshToken":"eyJhbG...","tokenType":"Bearer"}

# 3. 내 정보 조회 (accessToken 사용)
curl -X GET http://localhost:8080/api/user/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

# 4. 토큰 갱신
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Refresh-Token: eyJhbGciOiJIUzI1NiJ9..."
```

---

### 방법 3: Postman

1. Postman 실행
2. 새 요청 생성
3. URL 입력: `http://localhost:8080/api/auth/login`
4. Method: POST
5. Headers 탭: `Content-Type: application/json`
6. Body 탭 → raw → JSON 선택
7. 요청 본문 입력 후 Send

**Environment Variable 활용:**
- `{{baseUrl}}`: `http://localhost:8080`
- `{{accessToken}}`: 로그인 후 자동 저장

---

## 데이터베이스 확인

### SQL로 확인

```bash
mysql -u root -p jpa_jwt_db
```

```sql
-- 사용자 목록
SELECT * FROM users;

-- 리프레시 토큰 목록
SELECT * FROM refresh_tokens;

-- 특정 사용자의 토큰 확인
SELECT u.username, rt.token, rt.expired_at
FROM users u
LEFT JOIN refresh_tokens rt ON u.id = rt.user_id
WHERE u.username = 'testuser';
```

---

## JWT 토큰 디코딩

JWT 토큰은 Base64로 인코딩되어 있어 디코딩하여 내용을 확인할 수 있습니다.

**온라인 도구:**
- https://jwt.io/

**토큰 구조:**
```
Header.Payload.Signature
```

**Payload 예시:**
```json
{
  "sub": "testuser",
  "role": "ROLE_USER",
  "iat": 1701234567,
  "exp": 1701238167
}
```

- `sub`: Subject (사용자명)
- `role`: 권한
- `iat`: Issued At (발급 시간)
- `exp`: Expiration (만료 시간)

---

## 다음 단계

- API 테스트 완료 후 [TROUBLESHOOTING.md](TROUBLESHOOTING.md)를 참고하여 문제 해결
- 소스 코드를 열어 JWT 인증 플로우 이해하기
- 커스텀 API 엔드포인트 추가해보기

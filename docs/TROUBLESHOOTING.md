# 문제 해결 가이드

JPA JWT 프로젝트에서 자주 발생하는 문제와 해결 방법입니다.

## 📋 목차

1. [서버 시작 문제](#서버-시작-문제)
2. [데이터베이스 연결 문제](#데이터베이스-연결-문제)
3. [컴파일 오류](#컴파일-오류)
4. [인증 관련 문제](#인증-관련-문제)
5. [API 테스트 문제](#api-테스트-문제)
6. [자주 묻는 질문 (FAQ)](#자주-묻는-질문-faq)

---

## 서버 시작 문제

### 1. Connection refused: localhost:8080

**증상:**
```
io.netty.channel.AbstractChannel$AnnotatedConnectException:
Connection refused: localhost/[0:0:0:0:0:0:0:1]:8080
```

**원인:**
- Spring Boot 애플리케이션이 실행되지 않음
- 서버 시작 실패

**해결 방법:**

#### Step 1: 서버 실행 확인
```bash
# Maven으로 실행
mvn spring-boot:run

# 로그 확인 - 다음과 같은 메시지가 있어야 함
# Started JpaJwtApplication in X.XXX seconds
```

#### Step 2: 포트 충돌 확인
```bash
# 8080 포트 사용 중인 프로세스 확인
lsof -i :8080

# 프로세스 종료
kill -9 <PID>
```

#### Step 3: 애플리케이션 로그 확인
서버 시작 시 에러가 있는지 확인하세요. 주로 다음과 같은 문제가 발생합니다:
- MariaDB 연결 실패
- 의존성 문제
- 설정 오류

---

### 2. Port 8080 is already in use

**증상:**
```
Port 8080 was already in use.
```

**원인:**
- 다른 애플리케이션이 8080 포트 사용 중
- 이전 실행이 제대로 종료되지 않음

**해결 방법:**

#### 옵션 A: 기존 프로세스 종료
```bash
# 8080 포트 사용 프로세스 찾기
lsof -i :8080

# 프로세스 종료
kill -9 <PID>
```

#### 옵션 B: 다른 포트 사용
`application.properties` 수정:
```properties
server.port=8081
```

---

## 데이터베이스 연결 문제

### 1. Communications link failure

**증상:**
```
com.mysql.cj.jdbc.exceptions.CommunicationsException:
Communications link failure
```

**원인:**
- MariaDB 서버가 실행되지 않음
- 잘못된 연결 정보

**해결 방법:**

#### Step 1: MariaDB 서비스 확인
```bash
# macOS (Homebrew)
brew services list | grep mariadb

# MariaDB가 stopped 상태라면
brew services start mariadb

# 또는
mysql.server start
```

#### Step 2: 연결 테스트
```bash
# 직접 연결 시도
mysql -u root -p

# 특정 데이터베이스 연결
mysql -u root -p jpa_jwt_db
```

연결이 안 되면 비밀번호가 잘못되었거나 MariaDB가 실행되지 않은 것입니다.

#### Step 3: application.properties 확인
```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/jpa_jwt_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_actual_password
```

---

### 2. Access denied for user

**증상:**
```
java.sql.SQLException: Access denied for user 'root'@'localhost'
(using password: YES)
```

**원인:**
- 잘못된 사용자명 또는 비밀번호
- 권한 부족

**해결 방법:**

#### Step 1: 비밀번호 확인
```bash
# MariaDB 접속 테스트
mysql -u root -p
# 비밀번호 입력
```

접속이 안 되면 비밀번호 재설정:
```bash
# MariaDB 중지
brew services stop mariadb

# 안전 모드로 시작
mysqld_safe --skip-grant-tables &

# 다른 터미널에서
mysql -u root

# 비밀번호 재설정
USE mysql;
ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_password';
FLUSH PRIVILEGES;
EXIT;

# MariaDB 재시작
brew services restart mariadb
```

#### Step 2: application.properties 업데이트
```properties
spring.datasource.password=new_password
```

---

### 3. Unknown database 'jpa_jwt_db'

**증상:**
```
java.sql.SQLSyntaxErrorException: Unknown database 'jpa_jwt_db'
```

**원인:**
- 데이터베이스가 생성되지 않음
- `createDatabaseIfNotExist=true` 옵션이 작동하지 않음

**해결 방법:**

#### 수동으로 데이터베이스 생성
```bash
mysql -u root -p
```

```sql
CREATE DATABASE jpa_jwt_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SHOW DATABASES;
EXIT;
```

또는 application.properties의 URL에 옵션 확인:
```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/jpa_jwt_db?createDatabaseIfNotExist=true
```

---

## 컴파일 오류

### 1. ClassNotFoundException: hellojpa.entity.RefreshToken

**증상:**
```
java.lang.ClassNotFoundException: hellojpa.entity.RefreshToken
```

**원인:**
- 컴파일되지 않은 클래스
- target 폴더에 .class 파일 없음
- IntelliJ 캐시 문제

**해결 방법:**

#### Step 1: Maven Clean & Compile
```bash
mvn clean compile
```

#### Step 2: IntelliJ 재빌드
1. **Build** → **Rebuild Project**
2. 또는 **File** → **Invalidate Caches** → **Invalidate and Restart**

#### Step 3: 재실행
```bash
mvn spring-boot:run
```

---

### 2. ifPresentOrElse cannot be resolved

**증상:**
```
'Optional'의 메서드 'ifPresentOrElse'을(를) 해결할 수 없습니다
```

**원인:**
- Java 8 사용 중 (ifPresentOrElse는 Java 9+)
- pom.xml의 Java 버전이 낮음

**해결 방법:**

pom.xml 수정:
```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>
```

Maven 프로젝트 새로고침:
```bash
mvn clean compile
```

---

### 3. Lombok 관련 오류

**증상:**
```
cannot find symbol: method builder()
cannot find symbol: variable log
```

**원인:**
- Lombok 플러그인 미설치
- Annotation Processing 비활성화

**해결 방법:**

#### Step 1: Lombok 플러그인 설치
1. **IntelliJ** → **Preferences** → **Plugins**
2. "Lombok" 검색 및 설치
3. IntelliJ 재시작

#### Step 2: Annotation Processing 활성화
1. **Preferences** → **Build, Execution, Deployment** → **Compiler** → **Annotation Processors**
2. ✅ **Enable annotation processing** 체크

#### Step 3: 재빌드
```bash
mvn clean compile
```

---

## 인증 관련 문제

### 1. 401 Unauthorized (토큰 없음)

**증상:**
```
HTTP/1.1 401 Unauthorized
```

**원인:**
- Authorization 헤더 누락
- 잘못된 토큰 형식

**해결 방법:**

올바른 헤더 형식:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**주의사항:**
- "Bearer" 다음에 **공백** 필요
- 토큰 앞뒤 따옴표 제거
- 토큰 전체 복사 (잘리지 않도록)

curl 예시:
```bash
curl -X GET http://localhost:8080/api/user/me \
  -H "Authorization: Bearer YOUR_FULL_TOKEN_HERE"
```

---

### 2. 403 Forbidden (권한 없음)

**증상:**
```
HTTP/1.1 403 Forbidden
```

**원인:**
- ROLE_USER 권한으로 ROLE_ADMIN 엔드포인트 접근
- 유효하지 않은 토큰

**해결 방법:**

#### 권한 확인
토큰을 https://jwt.io/ 에서 디코딩하여 `role` 확인:
```json
{
  "sub": "testuser",
  "role": "ROLE_USER",  // <-- 여기 확인
  "iat": 1701234567,
  "exp": 1701238167
}
```

#### 관리자 권한이 필요한 경우
데이터베이스에서 직접 권한 변경:
```sql
UPDATE users SET role = 'ROLE_ADMIN' WHERE username = 'testuser';
```

---

### 3. 토큰 만료 (Token expired)

**증상:**
- API 호출 시 401 에러
- 로그인 후 1시간 경과

**원인:**
- Access Token 만료 (유효기간: 1시간)

**해결 방법:**

#### Refresh Token으로 재발급
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Refresh-Token: YOUR_REFRESH_TOKEN_HERE"
```

새로운 Access Token이 발급됩니다.

#### 또는 재로그인
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

---

## API 테스트 문제

### 1. IntelliJ HTTP Client가 작동하지 않음

**증상:**
- .http 파일에서 실행 버튼이 보이지 않음
- 요청이 실행되지 않음

**해결 방법:**

#### Step 1: 파일 확장자 확인
- 파일명이 `.http`로 끝나는지 확인
- 예: `api-test.http`

#### Step 2: IntelliJ 버전 확인
- IntelliJ IDEA 2022.1 이상 권장
- HTTP Client는 기본 내장

#### Step 3: 플러그인 확인
- **Preferences** → **Plugins**
- "HTTP Client" 플러그인이 활성화되어 있는지 확인

---

### 2. curl로 JSON 전송이 안 됨

**증상:**
```
curl: (3) URL using bad/illegal format or missing URL
```

**원인:**
- 따옴표 이스케이프 문제
- 줄바꿈 문제

**해결 방법:**

#### macOS/Linux
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

#### Windows (PowerShell)
```powershell
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"username\":\"testuser\",\"password\":\"password123\"}'
```

---

## 자주 묻는 질문 (FAQ)

### Q1: H2 대신 MariaDB를 사용하는 이유는?

**A:**
- H2는 인메모리 DB로 재시작 시 데이터 손실
- MariaDB는 실제 프로덕션 환경과 유사
- 영구 데이터 저장 가능
- 실무에서 많이 사용

---

### Q2: JWT Secret Key를 변경해야 하나요?

**A:**
네, 프로덕션 환경에서는 반드시 변경하세요.

```properties
# 최소 256비트 이상의 랜덤 문자열
jwt.secret=YourVeryLongAndSecureSecretKeyHere!!!
```

**생성 방법:**
```bash
# macOS/Linux
openssl rand -base64 64
```

---

### Q3: Access Token 유효기간을 변경하려면?

**A:**
`application.properties` 수정:

```properties
# 1시간 = 3600000ms
jwt.access-token-validity=3600000

# 30분으로 변경
jwt.access-token-validity=1800000
```

---

### Q4: 회원가입 시 자동으로 ROLE_ADMIN으로 설정하려면?

**A:**
`AuthService.java`의 `signup` 메서드 수정:

```java
User user = User.builder()
    .username(request.getUsername())
    .password(passwordEncoder.encode(request.getPassword()))
    .email(request.getEmail())
    .role(User.Role.ROLE_ADMIN)  // <- 변경
    .build();
```

---

### Q5: 데이터베이스를 초기화하려면?

**A:**

#### 방법 1: 테이블 삭제 (데이터만)
```sql
mysql -u root -p jpa_jwt_db

TRUNCATE TABLE refresh_tokens;
TRUNCATE TABLE users;
```

#### 방법 2: 테이블 재생성 (구조 변경 시)
```sql
DROP TABLE refresh_tokens;
DROP TABLE users;
```

애플리케이션 재시작 → JPA가 자동으로 재생성

#### 방법 3: 데이터베이스 전체 삭제
```sql
DROP DATABASE jpa_jwt_db;
CREATE DATABASE jpa_jwt_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

### Q6: 로그 레벨을 조정하려면?

**A:**
`application.properties`에서 조정:

```properties
# SQL 로그만 보기
logging.level.org.hibernate.SQL=DEBUG

# SQL + 파라미터 값 보기
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# 프로덕션에서는 INFO 레벨
logging.level.hellojpa=INFO
```

---

### Q7: IntelliJ에서 "Cannot resolve symbol" 에러

**A:**

#### Step 1: Maven 새로고침
1. 우측 Maven 탭 클릭
2. 새로고침 아이콘 (🔄) 클릭

#### Step 2: 캐시 정리
**File** → **Invalidate Caches** → **Invalidate and Restart**

#### Step 3: Reimport
```bash
mvn clean install
```

---

### Q8: API 테스트 시 한글이 깨져요

**A:**

#### application.properties 추가
```properties
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.force=true
```

#### curl 사용 시
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json; charset=UTF-8" \
  -d '{"username":"한글사용자","password":"password123","email":"test@test.com"}'
```

---

## 추가 도움이 필요하신가요?

위 방법으로도 해결되지 않는다면:

1. **로그 확인**: 콘솔에 출력되는 전체 에러 로그 확인
2. **스택 오버플로우**: 에러 메시지로 검색
3. **GitHub Issues**: Spring Boot 공식 저장소 이슈 검색

---

**관련 문서:**
- [환경 설정 가이드](SETUP.md)
- [API 가이드](API_GUIDE.md)
- [메인 README](../README.md)

# JPA + JWT 인증 학습 프로젝트

Spring Boot와 JPA를 활용한 JWT 기반 인증 시스템 구현 학습 프로젝트입니다.

## 📚 프로젝트 소개

이 프로젝트는 다음을 학습하기 위해 만들어졌습니다:
- **JPA (Java Persistence API)**: 엔티티 매핑, 연관관계, 영속성 컨텍스트
- **JWT (JSON Web Token)**: 토큰 기반 인증 및 권한 관리
- **Spring Security**: 보안 설정, 필터 체인, 인증/인가
- **Spring Data JPA**: Repository 패턴, 쿼리 메서드

## 🛠 기술 스택

- **Java 17**
- **Spring Boot 2.7.18**
- **Spring Data JPA**
- **Spring Security**
- **JWT (jjwt 0.11.5)**
- **MariaDB** (또는 H2 Database)
- **Lombok**
- **Maven**

## 📁 프로젝트 구조

```
src/main/java/hellojpa/
├── config/              # 설정 클래스
│   └── SecurityConfig.java
├── controller/          # REST API 컨트롤러
│   ├── AuthController.java
│   └── UserController.java
├── dto/                 # 데이터 전송 객체
│   ├── LoginRequest.java
│   ├── SignupRequest.java
│   ├── TokenResponse.java
│   └── UserResponse.java
├── entity/              # JPA 엔티티
│   ├── User.java
│   └── RefreshToken.java
├── repository/          # JPA Repository
│   ├── UserRepository.java
│   └── RefreshTokenRepository.java
├── security/            # 보안 관련 클래스
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
├── service/             # 비즈니스 로직
│   └── AuthService.java
└── JpaJwtApplication.java  # 메인 클래스
```

## 🚀 빠른 시작

### 1. 필수 요구사항

- Java 17 이상
- Maven 3.6 이상
- MariaDB 10.x 이상 (또는 H2 Database)

### 2. MariaDB 설정

```bash
# MariaDB 접속
mysql -u root -p

# 데이터베이스 생성
CREATE DATABASE jpa_jwt_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 사용자 생성 (선택사항)
CREATE USER 'jpauser'@'localhost' IDENTIFIED BY 'jpapassword';
GRANT ALL PRIVILEGES ON jpa_jwt_db.* TO 'jpauser'@'localhost';
FLUSH PRIVILEGES;
```

### 3. application.properties 설정

**중요: 보안 설정**

`application.properties.example` 파일을 복사하여 실제 설정 파일을 생성하세요:

```bash
# 템플릿 파일 복사
cp src/main/resources/application.properties.example \
   src/main/resources/application.properties
```

그런 다음 `application.properties` 파일의 DB 정보를 실제 값으로 수정하세요:

```properties
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password
jwt.secret=your_secure_secret_key
```

**⚠️ 주의:** `application.properties` 파일은 `.gitignore`에 포함되어 있어 Git에 커밋되지 않습니다.

### 4. 애플리케이션 실행

```bash
# Maven을 사용하여 실행
mvn spring-boot:run

# 또는 컴파일 후 실행
mvn clean package
java -jar target/ex1-hello-jpa-1.0-SNAPSHOT.jar
```

서버가 `http://localhost:8080`에서 실행됩니다.

## 📡 API 엔드포인트

### 인증 API (인증 불필요)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 (JWT 발급) |
| POST | `/api/auth/refresh` | 액세스 토큰 갱신 |
| POST | `/api/auth/logout` | 로그아웃 |

### 사용자 API (인증 필요)

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | `/api/user/me` | 내 정보 조회 | USER |
| GET | `/api/user/admin` | 관리자 전용 | ADMIN |

## 🧪 테스트 방법

### IntelliJ HTTP Client 사용

프로젝트 루트의 `api-test.http` 파일을 IntelliJ에서 열고 실행 버튼을 클릭하세요.

### curl 사용

```bash
# 1. 회원가입
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123","email":"test@example.com"}'

# 2. 로그인
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'

# 3. 내 정보 조회 (토큰 필요)
curl -X GET http://localhost:8080/api/user/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## 🔑 JWT 인증 플로우

```
1. 클라이언트가 username/password로 로그인 요청
   ↓
2. 서버가 인증 성공 시 Access Token + Refresh Token 발급
   ↓
3. 클라이언트가 Access Token을 헤더에 포함하여 API 요청
   ↓
4. JwtAuthenticationFilter가 토큰 검증 후 SecurityContext에 인증 정보 설정
   ↓
5. Access Token 만료 시 Refresh Token으로 재발급
```

## 📖 주요 학습 포인트

### JPA
- `@Entity`, `@Table`: 엔티티 매핑
- `@Id`, `@GeneratedValue`: 기본키 설정
- `@ManyToOne`, `@JoinColumn`: 연관관계 매핑
- `@PrePersist`, `@PreUpdate`: 생명주기 콜백
- `JpaRepository`: CRUD 메서드 자동 생성
- `@Transactional`: 트랜잭션 관리

### JWT
- **Access Token**: 짧은 유효기간 (1시간), API 인증용
- **Refresh Token**: 긴 유효기간 (7일), Access Token 재발급용
- HMAC-SHA256 알고리즘 사용
- 토큰에 사용자명과 권한 정보 포함

### Spring Security
- `WebSecurityConfigurerAdapter`: 보안 설정
- `OncePerRequestFilter`: JWT 검증 필터
- `UserDetailsService`: 사용자 정보 로드
- `PasswordEncoder`: BCrypt 비밀번호 암호화
- `AuthenticationManager`: 인증 처리

## 🔒 보안 주의사항

**Public 저장소 사용 시 반드시 지켜야 할 사항:**

1. ✅ **절대로 커밋하지 말 것**
   - 데이터베이스 비밀번호
   - JWT Secret Key (프로덕션용)
   - API 키, 토큰 등 민감한 정보

2. ✅ **`.gitignore`에 추가됨**
   - `application.properties` (실제 설정 파일)
   - `application-*.properties` (프로파일별 설정)

3. ✅ **안전하게 공유하는 방법**
   - `application.properties.example` 템플릿 사용
   - 환경 변수로 민감한 정보 관리
   - README에 설정 방법 문서화

4. ⚠️ **이미 커밋된 경우**
   - Git 히스토리에서 제거 필요
   - 비밀번호 즉시 변경
   - JWT Secret Key 변경

## 📚 추가 문서

- [환경 설정 가이드](docs/SETUP.md)
- [API 상세 가이드](docs/API_GUIDE.md)
- [문제 해결 가이드](docs/TROUBLESHOOTING.md)

## 🔧 문제 해결

자주 발생하는 문제는 [TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md)를 참고하세요.

### 빠른 해결 방법

**서버가 시작되지 않는 경우:**
```bash
mvn clean compile
mvn spring-boot:run
```

**MariaDB 연결 오류:**
- MariaDB 서비스 실행 확인: `mysql -u root -p`
- `application.properties`의 username/password 확인

## 📝 라이선스

학습 목적의 프로젝트입니다.

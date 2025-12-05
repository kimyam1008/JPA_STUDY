# 환경 설정 가이드

JPA JWT 프로젝트 실행을 위한 환경 설정 가이드입니다.

## 📋 목차

1. [Java 17 설치](#1-java-17-설치)
2. [Maven 설치](#2-maven-설치)
3. [MariaDB 설치 및 설정](#3-mariadb-설치-및-설정)
4. [프로젝트 설정](#4-프로젝트-설정)
5. [IntelliJ IDEA 설정](#5-intellij-idea-설정)

---

## 1. Java 17 설치

### macOS (Homebrew)

```bash
# OpenJDK 17 설치
brew install openjdk@17

# 심볼릭 링크 생성
sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk \
  /Library/Java/JavaVirtualMachines/openjdk-17.jdk

# 버전 확인
java -version
```

### 환경 변수 설정

`~/.zshrc` 또는 `~/.bash_profile`에 추가:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH=$JAVA_HOME/bin:$PATH
```

적용:
```bash
source ~/.zshrc
```

---

## 2. Maven 설치

### macOS (Homebrew)

```bash
# Maven 설치
brew install maven

# 버전 확인
mvn -version
```

---

## 3. MariaDB 설치 및 설정

### 3.1 MariaDB 설치

#### macOS (Homebrew)

```bash
# MariaDB 설치
brew install mariadb

# MariaDB 시작
brew services start mariadb

# 또는 일회성 실행
mysql.server start
```

#### MariaDB 상태 확인

```bash
brew services list | grep mariadb
# 또는
mysql.server status
```

### 3.2 초기 보안 설정

```bash
# 보안 설정 실행
mysql_secure_installation
```

다음 질문에 답변:
- **Enter current password for root**: (비어있으면 Enter)
- **Set root password?**: Y → 원하는 비밀번호 입력
- **Remove anonymous users?**: Y
- **Disallow root login remotely?**: Y
- **Remove test database?**: Y
- **Reload privilege tables now?**: Y

### 3.3 데이터베이스 생성

```bash
# MariaDB 접속
mysql -u root -p
# 비밀번호 입력
```

SQL 명령어 실행:

```sql
-- 데이터베이스 생성
CREATE DATABASE jpa_jwt_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 전용 사용자 생성 (선택사항, 보안 강화)
CREATE USER IF NOT EXISTS 'jpauser'@'localhost' IDENTIFIED BY 'jpapassword';

-- 권한 부여
GRANT ALL PRIVILEGES ON jpa_jwt_db.* TO 'jpauser'@'localhost';

-- 권한 적용
FLUSH PRIVILEGES;

-- 데이터베이스 목록 확인
SHOW DATABASES;

-- 종료
EXIT;
```

### 3.4 연결 테스트

```bash
# root 계정으로 접속
mysql -u root -p jpa_jwt_db

# 또는 생성한 사용자로 접속
mysql -u jpauser -p jpa_jwt_db
```

---

## 4. 프로젝트 설정

### 4.1 프로젝트 클론 또는 다운로드

```bash
cd ~/your-workspace
git clone <repository-url>
cd JPA_STUDY
```

### 4.2 application.properties 설정

`src/main/resources/application.properties` 파일을 열고 데이터베이스 정보를 수정하세요:

```properties
# Server Port
server.port=8080

# MariaDB Database Configuration
spring.datasource.url=jdbc:mariadb://localhost:3306/jpa_jwt_db?createDatabaseIfNotExist=true
spring.datasource.driverClassName=org.mariadb.jdbc.Driver

# root 계정 사용 시
spring.datasource.username=root
spring.datasource.password=your_root_password

# 또는 전용 사용자 사용 시 (권장)
# spring.datasource.username=jpauser
# spring.datasource.password=jpapassword

# JPA Configuration
spring.jpa.database-platform=org.hibernate.dialect.MariaDBDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true

# JWT Configuration
jwt.secret=yourSecretKeyHereMustBeLongerThan256BitsForHS256Algorithm!!
jwt.access-token-validity=3600000
jwt.refresh-token-validity=604800000

# Logging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.level.hellojpa=DEBUG
```

### 4.3 의존성 다운로드 및 컴파일

```bash
# 의존성 다운로드 및 컴파일
mvn clean compile

# 또는 패키지 빌드
mvn clean package
```

### 4.4 애플리케이션 실행

```bash
# Maven으로 실행
mvn spring-boot:run

# 또는 JAR 파일 실행
java -jar target/ex1-hello-jpa-1.0-SNAPSHOT.jar
```

성공적으로 실행되면 다음과 같은 로그를 볼 수 있습니다:

```
Started JpaJwtApplication in 3.456 seconds
```

---

## 5. IntelliJ IDEA 설정

### 5.1 프로젝트 열기

1. IntelliJ IDEA 실행
2. **Open** 클릭
3. 프로젝트 폴더 선택 (`pom.xml`이 있는 폴더)
4. **Open as Project** 선택

### 5.2 SDK 설정

1. **File** → **Project Structure** (Cmd+;)
2. **Project Settings** → **Project**
3. **SDK**: Java 17 선택
4. **Language level**: 17 선택

### 5.3 Maven 자동 임포트 설정

1. **IntelliJ IDEA** → **Preferences** (Cmd+,)
2. **Build, Execution, Deployment** → **Build Tools** → **Maven**
3. **Importing** 탭
4. ✅ **Import Maven projects automatically** 체크

### 5.4 Annotation Processing 활성화 (Lombok)

1. **IntelliJ IDEA** → **Preferences**
2. **Build, Execution, Deployment** → **Compiler** → **Annotation Processors**
3. ✅ **Enable annotation processing** 체크

### 5.5 Lombok 플러그인 설치

1. **IntelliJ IDEA** → **Preferences**
2. **Plugins**
3. "Lombok" 검색 및 설치
4. IntelliJ 재시작

### 5.6 애플리케이션 실행 설정

#### 방법 1: Main 클래스 직접 실행
1. `JpaJwtApplication.java` 파일 열기
2. `main` 메서드 옆의 ▶️ 아이콘 클릭
3. **Run 'JpaJwtApplication'** 선택

#### 방법 2: Maven Run Configuration
1. **Run** → **Edit Configurations**
2. **+** 클릭 → **Maven** 선택
3. **Name**: Spring Boot Run
4. **Command line**: `spring-boot:run`
5. **OK**

---

## 6. 데이터베이스 확인 도구

### 6.1 명령줄 (mysql 클라이언트)

```bash
# 데이터베이스 접속
mysql -u root -p jpa_jwt_db

# 테이블 목록 확인
SHOW TABLES;

# users 테이블 구조 확인
DESCRIBE users;

# 데이터 조회
SELECT * FROM users;
SELECT * FROM refresh_tokens;

# 종료
EXIT;
```

### 6.2 GUI 도구 (선택사항)

#### MySQL Workbench
- 다운로드: https://dev.mysql.com/downloads/workbench/
- MariaDB와 완벽 호환

#### DBeaver (추천)
- 다운로드: https://dbeaver.io/
- 무료, 오픈소스, 다중 데이터베이스 지원

**연결 정보:**
- Host: localhost
- Port: 3306
- Database: jpa_jwt_db
- Username: root (또는 jpauser)
- Password: (설정한 비밀번호)

#### IntelliJ Database Tool (Ultimate 버전만)
1. **View** → **Tool Windows** → **Database**
2. **+** → **Data Source** → **MariaDB**
3. 연결 정보 입력
4. **Test Connection** → **OK**

---

## 7. 환경 변수 설정 (프로덕션)

민감한 정보는 환경 변수로 관리하는 것이 좋습니다.

### 7.1 환경 변수 설정

```bash
export DB_USERNAME=jpauser
export DB_PASSWORD=jpapassword
export JWT_SECRET=yourSecretKey...
```

### 7.2 application.properties 수정

```properties
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
```

---

## 8. 문제 해결

### MariaDB 접속 오류

**증상:**
```
Access denied for user 'root'@'localhost'
```

**해결:**
```bash
# 비밀번호 재설정
sudo mysql
ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_password';
FLUSH PRIVILEGES;
EXIT;
```

### MariaDB 서비스가 시작되지 않음

**macOS:**
```bash
# 서비스 상태 확인
brew services list

# 서비스 재시작
brew services restart mariadb

# 수동 시작
mysql.server start
```

### Port 3306 이미 사용 중

```bash
# 3306 포트 사용 프로세스 확인
lsof -i :3306

# 프로세스 종료
kill -9 <PID>
```

---

## 9. 다음 단계

환경 설정이 완료되었다면:
1. [API 가이드](API_GUIDE.md)를 참고하여 API 테스트
2. [문제 해결 가이드](TROUBLESHOOTING.md)를 북마크
3. `api-test.http` 파일로 실제 API 호출 테스트

---

**설정 중 문제가 발생하면 [TROUBLESHOOTING.md](TROUBLESHOOTING.md)를 참고하세요.**

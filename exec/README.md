# SSABRE 프로젝트 실행 가이드

## 📋 목차
1. [사전 요구사항](#사전-요구사항)
2. [환경 설정](#환경-설정)
3. [DB 덤프 백업 및 복원](#db-덤프-백업-및-복원)
4. [로컬 실행 방법](#로컬-실행-방법)
5. [서버 배포 방법 (Linux)](#서버-배포-방법-linux)
6. [모니터링](#모니터링)
7. [문제 해결](#문제-해결)

---

## 🔧 사전 요구사항

### 로컬 개발 환경
- **Docker** & **Docker Compose** 설치
- **Git** 설치
- **Node.js** (v18 이상) - 프론트엔드 개발 시
- **Java 17** - 백엔드 개발 시

### 서버 환경 (Linux)
- **Docker** & **Docker Compose** 설치
- **Git** 설치
- 최소 2GB RAM 권장
- 최소 10GB 디스크 공간

---

## ⚙️ 환경 설정

### 1. `.env` 파일 생성

프로젝트 루트 디렉토리에 `.env` 파일을 생성하고 다음 내용을 입력합니다:

```bash
# Database
POSTGRES_DB=ssabre_db
POSTGRES_USER=ssabre_user
POSTGRES_PASSWORD=your_secure_password_here

# File Upload
FILE_BASE_URL=http://your-server-ip/uploads

# Mattermost Webhooks (선택사항)
MM_14th_WEBHOOK_URL=https://your-mattermost-webhook-url-14th
MM_15th_WEBHOOK_URL=https://your-mattermost-webhook-url-15th

# AI API Keys (선택사항)
AI_GEMINI_API_KEY=your_gemini_api_key
AI_GMS_API_KEY=your_gms_api_key
```

**⚠️ 주의사항:**
- `POSTGRES_PASSWORD`는 반드시 강력한 비밀번호로 설정하세요
- `FILE_BASE_URL`은 실제 서버 IP 또는 도메인으로 변경하세요
- 민감한 정보이므로 `.env` 파일은 절대 Git에 커밋하지 마세요

---

## 💾 DB 덤프 백업 및 복원

### DB 백업 (Dump)

서버에서 PostgreSQL 데이터베이스를 백업하는 방법:

#### 방법 1: Docker 컨테이너 사용 (권장)

```bash
# 1. 백업 디렉토리 생성
mkdir -p ~/ssabre_backups

# 2. DB 덤프 생성
docker exec ssabre_postgres pg_dump -U ssabre_user ssabre_db > ~/ssabre_backups/ssabre_db_$(date +%Y%m%d_%H%M%S).sql

# 3. 압축 (선택사항)
gzip ~/ssabre_backups/ssabre_db_$(date +%Y%m%d_%H%M%S).sql
```

#### 방법 2: 자동 백업 스크립트 사용

프로젝트에 포함된 백업 스크립트 사용:

```bash
# 백업 스크립트 실행 권한 부여
chmod +x scripts/backup-db.sh

# 백업 실행
./scripts/backup-db.sh
```

### DB 복원 (Restore)

백업 파일에서 데이터베이스를 복원하는 방법:

#### 압축 해제 (필요한 경우)
```bash
gunzip ~/ssabre_backups/ssabre_db_20260208_230000.sql.gz
```

#### 복원 실행
```bash
# Docker 컨테이너로 복원
cat ~/ssabre_backups/ssabre_db_20260208_230000.sql | docker exec -i ssabre_postgres psql -U ssabre_user -d ssabre_db
```

**⚠️ 복원 시 주의사항:**
- 복원 전에 현재 데이터를 백업하세요
- 복원은 기존 데이터를 덮어쓸 수 있습니다
- 가능하면 테스트 환경에서 먼저 복원을 테스트하세요

### 원격 백업 전송 (Windows → Linux 서버)

Windows에서 Linux 서버로 백업 파일 전송:

```powershell
# SCP를 사용한 전송 (Git Bash 또는 PowerShell에서)
scp user@server-ip:~/ssabre_backups/ssabre_db_20260208_230000.sql.gz ./backups/
```

Linux 서버에서 Windows로 백업 파일 다운로드:

```bash
# 서버에서 실행
scp ~/ssabre_backups/ssabre_db_20260208_230000.sql.gz user@windows-ip:/path/to/destination/
```

---

## 🚀 로컬 실행 방법

### 1. 프로젝트 클론

```bash
git clone <repository-url>
cd S14P11D103
```

### 2. 환경 변수 설정

위의 [환경 설정](#환경-설정) 섹션을 참고하여 `.env` 파일을 생성합니다.

### 3. Docker 이미지 빌드 및 실행

```bash
# 모든 서비스 빌드 및 실행
docker compose up -d --build

# 또는 개별 서비스만 빌드
docker compose build backend
docker compose build frontend
```

### 4. 서비스 확인

```bash
# 실행 중인 컨테이너 확인
docker compose ps

# 로그 확인
docker compose logs -f

# 특정 서비스 로그만 확인
docker compose logs -f backend
docker compose logs -f frontend
```

### 5. 접속

- **프론트엔드**: http://localhost
- **백엔드 API**: http://localhost/api
- **Grafana 대시보드**: http://localhost:3001 (admin / `.env`의 `GRAFANA_ADMIN_PASSWORD`)
- **Prometheus**: http://localhost/prometheus
- **NetData**: http://localhost/netdata

---

## 🌐 서버 배포 방법 (Linux)

### 1. 서버 접속

```bash
ssh user@your-server-ip
```

### 2. 프로젝트 디렉토리로 이동 또는 클론

```bash
# 처음 배포하는 경우
git clone <repository-url>
cd S14P11D103

# 이미 클론되어 있는 경우
cd S14P11D103
git pull origin main
```

### 3. 환경 변수 설정

```bash
# .env 파일 생성 및 편집
nano .env
# 또는
vi .env
```

위의 [환경 설정](#환경-설정) 섹션의 내용을 입력합니다.

**서버 IP 관련 설정 주의:**
- `FILE_BASE_URL`: 실제 서비스 도메인으로 변경 (예: `https://your-domain.example/uploads`)
- `docker-compose.yml`의 Grafana와 Prometheus 설정도 서버 IP에 맞게 수정

### 4. Docker 이미지 준비

#### 방법 1: GitLab CI/CD 사용 (권장)

프로젝트는 `.gitlab-ci.yml`을 통해 자동으로 이미지를 빌드하도록 설정되어 있습니다.

GitLab Registry에서 이미지를 pull:

```bash
docker login registry.gitlab.com

# 백엔드 이미지 pull
docker pull registry.gitlab.com/your-namespace/ssabre_backend:latest
docker tag registry.gitlab.com/your-namespace/ssabre_backend:latest ssabre_backend:latest

# 프론트엔드 이미지 pull
docker pull registry.gitlab.com/your-namespace/ssabre_frontend:latest
docker tag registry.gitlab.com/your-namespace/ssabre_frontend:latest ssabre_frontend:latest
```

#### 방법 2: 서버에서 직접 빌드

```bash
# docker-compose.yml의 build 부분 주석 해제 필요
docker compose build
```

### 5. 서비스 실행

```bash
# 백그라운드에서 실행
docker compose up -d

# 로그 확인
docker compose logs -f
```

### 6. 서비스 상태 확인

```bash
# 실행 중인 컨테이너 확인
docker compose ps

# 헬스체크 확인
docker inspect ssabre_backend | grep -A 10 Health
```

### 7. 방화벽 설정 (필요한 경우)

```bash
# Ubuntu/Debian
sudo ufw allow 80/tcp
sudo ufw allow 3001/tcp

# 또는 모든 포트 허용 (개발 환경)
sudo ufw disable
```

---

## 📊 모니터링

### Grafana 대시보드

1. 접속: `http://your-server-ip:3001` 또는 `http://your-server-ip/grafana`
2. 로그인: 
   - Username: `admin`
   - Password: `.env`의 `GRAFANA_ADMIN_PASSWORD` 값
3. Loki를 통한 로그 확인
4. Prometheus를 통한 메트릭 확인

### NetData 실시간 모니터링

- 접속: `http://your-server-ip/netdata`
- 서버 리소스 실시간 모니터링 (CPU, 메모리, 네트워크 등)

### Docker 로그 확인

```bash
# 모든 서비스 로그
docker compose logs -f

# 특정 서비스 로그
docker compose logs -f backend
docker compose logs -f postgres

# 최근 100줄만 보기
docker compose logs --tail=100 backend
```

---

## 🔧 문제 해결

### 서비스가 시작되지 않는 경우

```bash
# 1. 컨테이너 상태 확인
docker compose ps

# 2. 로그 확인
docker compose logs <service-name>

# 3. 컨테이너 재시작
docker compose restart <service-name>

# 4. 전체 재시작
docker compose down
docker compose up -d
```

### DB 연결 문제

```bash
# PostgreSQL 컨테이너 접속
docker exec -it ssabre_postgres psql -U ssabre_user -d ssabre_db

# DB 연결 테스ト
\conninfo
\dt  # 테이블 목록 확인
```

### 디스크 공간 부족

```bash
# 사용하지 않는 Docker 리소스 정리
docker system prune -a

# 볼륨 정리 (주의: 데이터 삭제됨)
docker volume prune
```

### 이미지 재빌드

```bash
# 캐시 없이 재빌드
docker compose build --no-cache

# 특정 서비스만 재빌드
docker compose build --no-cache backend
```

### 포트 충돌

```bash
# 포트 사용 중인 프로세스 확인 (Linux)
sudo netstat -tulpn | grep :80
sudo lsof -i :80

# 포트 사용 중인 프로세스 확인 (Windows)
netstat -ano | findstr :80
```

---

## 🛑 서비스 중지 및 제거

### 서비스 중지

```bash
docker compose stop
```

### 서비스 중지 및 컨테이너 제거

```bash
docker compose down
```

### 볼륨까지 모두 제거 (데이터 삭제)

```bash
docker compose down -v
```

---

## 📝 추가 정보

### 프로젝트 구조

```
S14P11D103/
├── backend/           # Spring Boot 백엔드
├── frontend/          # React 프론트엔드
├── monitoring/        # Loki, Prometheus 설정
├── nginx/             # Nginx 설정
├── uploads/           # 업로드 파일 저장소
├── docker-compose.yml # Docker Compose 설정
├── .env              # 환경 변수 (생성 필요)
└── 실행가이드.md      # 이 파일
```

### 환경별 설정

- **개발 환경**: 로컬에서 `docker compose up`
- **프로덕션 환경**: GitLab CI/CD를 통한 자동 배포
- **테스트 환경**: 별도 서버에 수동 배포

### 도움말

문제가 발생하면 다음을 확인하세요:
1. Docker 로그: `docker compose logs -f`
2. 환경 변수 설정 확인: `.env` 파일
3. 네트워크 연결 확인
4. 방화벽 설정 확인

---

**작성일**: 2026-02-08  
**버전**: 1.0

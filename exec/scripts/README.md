# SSABRE Scripts

이 디렉토리는 SSABRE 프로젝트의 유틸리티 스크립트를 포함합니다.

## 스크립트 목록

### 1. backup-db.sh
PostgreSQL 데이터베이스 백업 스크립트

**기능:**
- Docker 컨테이너에서 PostgreSQL DB 덤프 생성
- gzip 압축 지원
- 원격 서버로 전송 (SCP)
- 30일 이상 된 백업 파일 자동 정리

**사용법:**
```bash
# 기본 백업
./backup-db.sh

# 압축 백업
./backup-db.sh -c

# 압축 후 원격 전송
./backup-db.sh -c -r -d user@server:/backup
```

### 2. restore-db.sh
PostgreSQL 데이터베이스 복원 스크립트

**기능:**
- 백업 파일에서 DB 복원
- 복원 전 안전 백업 자동 생성
- 압축 파일 (.gz) 자동 해제
- 복원 후 검증

**사용법:**
```bash
# SQL 파일에서 복원
./restore-db.sh ~/ssabre_backups/ssabre_db_20260208_230000.sql

# 압축 파일에서 복원
./restore-db.sh ~/ssabre_backups/ssabre_db_20260208_230000.sql.gz
```

**주의:** 복원은 기존 데이터를 덮어씁니다. 자동으로 안전 백업이 생성되지만, 중요한 데이터는 별도로 백업하세요.

### 3. deploy.sh
통합 배포 스크립트 (Linux 서버용)

**기능:**
- Git pull 자동화
- 배포 전 DB 백업
- Docker 이미지 재빌드
- 서비스 재시작
- 헬스체크 자동 수행

**사용법:**
```bash
# 기본 배포 (재시작만)
./deploy.sh

# DB 백업 후 배포
./deploy.sh -b

# Git pull + DB 백업 + 재빌드 + 배포 (전체 업데이트)
./deploy.sh -b -p -r
```

## 리눅스 서버에서 사용하기

### 1. 실행 권한 부여
```bash
chmod +x scripts/*.sh
```

### 2. 백업 스크립트 실행
```bash
cd /path/to/S14P11D103
./scripts/backup-db.sh -c
```

### 3. 배포 스크립트 실행
```bash
cd /path/to/S14P11D103
./scripts/deploy.sh -b -p
```

## 정기 백업 설정 (Cron)

리눅스 서버에서 매일 자정에 자동 백업:

```bash
# crontab 편집
crontab -e

# 다음 줄 추가 (매일 자정)
0 0 * * * /path/to/S14P11D103/scripts/backup-db.sh -c

# 또는 매일 오전 3시
0 3 * * * /path/to/S14P11D103/scripts/backup-db.sh -c
```

## 문제 해결

### 스크립트 실행 시 권한 오류
```bash
chmod +x scripts/backup-db.sh
chmod +x scripts/restore-db.sh
chmod +x scripts/deploy.sh
```

### Docker 컨테이너를 찾을 수 없음
```bash
# 컨테이너 확인
docker ps

# 컨테이너 시작
docker compose up -d postgres
```

### 백업 디렉토리 확인
```bash
ls -lh ~/ssabre_backups/
```

## 노트

- 모든 스크립트는 `set -e` 옵션을 사용하여 에러 발생 시 즉시 중단됩니다.
- 백업 파일은 기본적으로 `~/ssabre_backups/` 디렉토리에 저장됩니다.
- 30일 이상 된 백업 파일은 자동으로 삭제됩니다.
- 복원 시 자동으로 안전 백업이 `~/ssabre_backups/safety/`에 생성됩니다.

---

**작성일**: 2026-02-08  
**버전**: 1.0

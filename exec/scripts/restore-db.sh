#!/bin/bash

###############################################################################
# SSABRE PostgreSQL Database Restore Script
# 
# 사용법:
#   ./restore-db.sh <backup_file>
#
# 예시:
#   ./restore-db.sh ~/ssabre_backups/ssabre_db_20260208_230000.sql
#   ./restore-db.sh ~/ssabre_backups/ssabre_db_20260208_230000.sql.gz
###############################################################################

set -e  # 에러 발생 시 스크립트 중단

# 설정
CONTAINER_NAME="ssabre_postgres"
DB_NAME="ssabre_db"
DB_USER="ssabre_user"
BACKUP_FILE="$1"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 도움말 함수
show_help() {
    echo "SSABRE PostgreSQL Database Restore Script"
    echo ""
    echo "사용법: $0 <backup_file>"
    echo ""
    echo "예시:"
    echo "  $0 ~/ssabre_backups/ssabre_db_20260208_230000.sql"
    echo "  $0 ~/ssabre_backups/ssabre_db_20260208_230000.sql.gz"
    exit 0
}

# 로그 함수
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_question() {
    echo -e "${BLUE}[?]${NC} $1"
}

# 인자 확인
if [ -z "$BACKUP_FILE" ]; then
    log_error "백업 파일을 지정해주세요."
    show_help
fi

if [ "$BACKUP_FILE" = "-h" ] || [ "$BACKUP_FILE" = "--help" ]; then
    show_help
fi

# 파일 존재 확인
if [ ! -f "$BACKUP_FILE" ]; then
    log_error "백업 파일을 찾을 수 없습니다: $BACKUP_FILE"
    exit 1
fi

# 컨테이너 확인
check_container() {
    if ! docker ps | grep -q "$CONTAINER_NAME"; then
        log_error "PostgreSQL 컨테이너 '$CONTAINER_NAME'가 실행 중이 아닙니다."
        log_info "다음 명령어로 컨테이너를 시작하세요: docker compose up -d postgres"
        exit 1
    fi
}

# 백업 생성 (복원 전)
create_safety_backup() {
    log_warn "복원 전 현재 데이터베이스를 백업합니다..."
    
    SAFETY_BACKUP_DIR="$HOME/ssabre_backups/safety"
    mkdir -p "$SAFETY_BACKUP_DIR"
    
    SAFETY_BACKUP_FILE="$SAFETY_BACKUP_DIR/safety_backup_$(date +%Y%m%d_%H%M%S).sql"
    
    if docker exec "$CONTAINER_NAME" pg_dump -U "$DB_USER" "$DB_NAME" > "$SAFETY_BACKUP_FILE"; then
        log_info "안전 백업 완료: $SAFETY_BACKUP_FILE"
        gzip "$SAFETY_BACKUP_FILE"
        log_info "안전 백업 압축 완료: ${SAFETY_BACKUP_FILE}.gz"
    else
        log_error "안전 백업 실패!"
        exit 1
    fi
}

# 데이터베이스 초기화 확인
confirm_restore() {
    log_warn ""
    log_warn "======================================"
    log_warn "  경고: 데이터베이스 복원"
    log_warn "======================================"
    log_warn ""
    log_warn "복원할 파일: $BACKUP_FILE"
    log_warn "대상 데이터베이스: $DB_NAME"
    log_warn ""
    log_warn "이 작업은 현재 데이터베이스의 모든 데이터를"
    log_warn "백업 파일의 데이터로 덮어씁니다."
    log_warn ""
    
    read -p "$(echo -e ${BLUE}계속하시겠습니까? [y/N]: ${NC})" -n 1 -r
    echo
    
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_info "복원이 취소되었습니다."
        exit 0
    fi
}

# 압축 해제 (필요한 경우)
decompress_if_needed() {
    if [[ "$BACKUP_FILE" == *.gz ]]; then
        log_info "압축 파일 감지, 압축 해제 중..."
        
        TEMP_FILE="${BACKUP_FILE%.gz}"
        
        if gunzip -c "$BACKUP_FILE" > "$TEMP_FILE"; then
            log_info "압축 해제 완료: $TEMP_FILE"
            BACKUP_FILE="$TEMP_FILE"
            TEMP_CREATED=true
        else
            log_error "압축 해제 실패!"
            exit 1
        fi
    fi
}

# 데이터베이스 복원
perform_restore() {
    log_info "데이터베이스 복원 시작..."
    log_info "백업 파일: $BACKUP_FILE"
    log_info "데이터베이스: $DB_NAME"
    
    # 기존 연결 종료
    log_info "기존 데이터베이스 연결 종료 중..."
    docker exec "$CONTAINER_NAME" psql -U "$DB_USER" -d postgres -c \
        "SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE pg_stat_activity.datname = '$DB_NAME' AND pid <> pg_backend_pid();" \
        2>/dev/null || true
    
    # 데이터베이스 삭제 및 재생성
    log_info "데이터베이스 재생성 중..."
    docker exec "$CONTAINER_NAME" psql -U "$DB_USER" -d postgres -c "DROP DATABASE IF EXISTS $DB_NAME;" || true
    docker exec "$CONTAINER_NAME" psql -U "$DB_USER" -d postgres -c "CREATE DATABASE $DB_NAME;"
    
    # 복원 실행
    log_info "데이터 복원 중... (시간이 걸릴 수 있습니다)"
    
    if cat "$BACKUP_FILE" | docker exec -i "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" > /dev/null 2>&1; then
        log_info "복원 완료!"
    else
        log_error "복원 실패!"
        log_error "안전 백업에서 복구를 시도하세요."
        exit 1
    fi
}

# 임시 파일 정리
cleanup() {
    if [ "$TEMP_CREATED" = true ] && [ -f "$TEMP_FILE" ]; then
        log_info "임시 파일 삭제 중..."
        rm -f "$TEMP_FILE"
    fi
}

# 복원 후 검증
verify_restore() {
    log_info "복원 검증 중..."
    
    # 테이블 수 확인
    TABLE_COUNT=$(docker exec "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';" | tr -d ' ')
    
    log_info "복원된 테이블 수: $TABLE_COUNT"
    
    if [ "$TABLE_COUNT" -eq 0 ]; then
        log_warn "테이블이 없습니다. 백업 파일을 확인하세요."
    else
        log_info "데이터베이스가 정상적으로 복원되었습니다."
    fi
}

# 테이블 목록 표시
show_tables() {
    log_info "복원된 테이블 목록:"
    docker exec "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" -c "\dt" 2>/dev/null || log_warn "테이블 목록을 가져올 수 없습니다."
}

# 메인 실행
main() {
    echo "======================================"
    echo "  SSABRE DB Restore Script"
    echo "  시작 시간: $(date '+%Y-%m-%d %H:%M:%S')"
    echo "======================================"
    echo ""
    
    check_container
    confirm_restore
    create_safety_backup
    decompress_if_needed
    perform_restore
    verify_restore
    cleanup
    
    echo ""
    show_tables
    
    echo ""
    echo "======================================"
    echo "  복원 완료!"
    echo "  종료 시간: $(date '+%Y-%m-%d %H:%M:%S')"
    echo "======================================"
    echo ""
    log_info "백엔드 서비스를 재시작하는 것을 권장합니다:"
    log_info "  docker compose restart backend"
}

# 스크립트 시작
main

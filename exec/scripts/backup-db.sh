#!/bin/bash

###############################################################################
# SSABRE PostgreSQL Database Backup Script
# 
# 사용법:
#   ./backup-db.sh [OPTIONS]
#
# 옵션:
#   -h, --help           도움말 표시
#   -c, --compress       백업 파일 압축 (gzip)
#   -r, --remote         원격 서버로 전송
#   -d, --destination    원격 서버 주소 (예: user@server:/path)
#
# 예시:
#   ./backup-db.sh                           # 기본 백업
#   ./backup-db.sh -c                        # 압축 백업
#   ./backup-db.sh -c -r -d user@server:/backup  # 압축 후 원격 전송
###############################################################################

set -e  # 에러 발생 시 스크립트 중단

# 설정
CONTAINER_NAME="ssabre_postgres"
DB_NAME="ssabre_db"
DB_USER="ssabre_user"
BACKUP_DIR="$HOME/ssabre_backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="ssabre_db_${TIMESTAMP}.sql"
COMPRESS=false
REMOTE=false
DESTINATION=""

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 도움말 함수
show_help() {
    echo "SSABRE PostgreSQL Database Backup Script"
    echo ""
    echo "사용법: $0 [OPTIONS]"
    echo ""
    echo "옵션:"
    echo "  -h, --help           도움말 표시"
    echo "  -c, --compress       백업 파일 압축 (gzip)"
    echo "  -r, --remote         원격 서버로 전송"
    echo "  -d, --destination    원격 서버 주소 (예: user@server:/path)"
    echo ""
    echo "예시:"
    echo "  $0                                    # 기본 백업"
    echo "  $0 -c                                 # 압축 백업"
    echo "  $0 -c -r -d user@server:/backup       # 압축 후 원격 전송"
    exit 0
}

# 인자 파싱
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            ;;
        -c|--compress)
            COMPRESS=true
            shift
            ;;
        -r|--remote)
            REMOTE=true
            shift
            ;;
        -d|--destination)
            DESTINATION="$2"
            shift 2
            ;;
        *)
            echo -e "${RED}알 수 없는 옵션: $1${NC}"
            show_help
            ;;
    esac
done

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

# 컨테이너 확인
check_container() {
    if ! docker ps | grep -q "$CONTAINER_NAME"; then
        log_error "PostgreSQL 컨테이너 '$CONTAINER_NAME'가 실행 중이 아닙니다."
        log_info "다음 명령어로 컨테이너를 시작하세요: docker compose up -d postgres"
        exit 1
    fi
}

# 백업 디렉토리 생성
create_backup_dir() {
    if [ ! -d "$BACKUP_DIR" ]; then
        log_info "백업 디렉토리 생성: $BACKUP_DIR"
        mkdir -p "$BACKUP_DIR"
    fi
}

# DB 백업 실행
perform_backup() {
    log_info "데이터베이스 백업 시작..."
    log_info "컨테이너: $CONTAINER_NAME"
    log_info "데이터베이스: $DB_NAME"
    log_info "사용자: $DB_USER"
    
    if docker exec "$CONTAINER_NAME" pg_dump -U "$DB_USER" "$DB_NAME" > "$BACKUP_DIR/$BACKUP_FILE"; then
        log_info "백업 완료: $BACKUP_DIR/$BACKUP_FILE"
        
        # 파일 크기 확인
        FILE_SIZE=$(du -h "$BACKUP_DIR/$BACKUP_FILE" | cut -f1)
        log_info "백업 파일 크기: $FILE_SIZE"
    else
        log_error "백업 실패!"
        exit 1
    fi
}

# 백업 파일 압축
compress_backup() {
    if [ "$COMPRESS" = true ]; then
        log_info "백업 파일 압축 중..."
        
        if gzip "$BACKUP_DIR/$BACKUP_FILE"; then
            BACKUP_FILE="${BACKUP_FILE}.gz"
            log_info "압축 완료: $BACKUP_DIR/$BACKUP_FILE"
            
            # 압축 후 파일 크기
            FILE_SIZE=$(du -h "$BACKUP_DIR/$BACKUP_FILE" | cut -f1)
            log_info "압축 파일 크기: $FILE_SIZE"
        else
            log_error "압축 실패!"
            exit 1
        fi
    fi
}

# 원격 서버로 전송
transfer_remote() {
    if [ "$REMOTE" = true ]; then
        if [ -z "$DESTINATION" ]; then
            log_error "원격 전송을 위해서는 -d 옵션으로 목적지를 지정해야 합니다."
            show_help
        fi
        
        log_info "원격 서버로 전송 중: $DESTINATION"
        
        if scp "$BACKUP_DIR/$BACKUP_FILE" "$DESTINATION"; then
            log_info "전송 완료!"
        else
            log_error "전송 실패!"
            exit 1
        fi
    fi
}

# 오래된 백업 파일 정리 (30일 이상)
cleanup_old_backups() {
    log_info "30일 이상 된 백업 파일 정리 중..."
    
    DELETED_COUNT=$(find "$BACKUP_DIR" -name "ssabre_db_*.sql*" -type f -mtime +30 -delete -print | wc -l)
    
    if [ "$DELETED_COUNT" -gt 0 ]; then
        log_info "삭제된 백업 파일: $DELETED_COUNT 개"
    else
        log_info "삭제할 오래된 백업 파일이 없습니다."
    fi
}

# 백업 목록 표시
show_backup_list() {
    log_info "최근 백업 파일 목록 (최신 5개):"
    ls -lht "$BACKUP_DIR"/ssabre_db_*.sql* 2>/dev/null | head -5 | awk '{print "  " $9 " (" $5 ", " $6 " " $7 " " $8 ")"}'
}

# 메인 실행
main() {
    echo "======================================"
    echo "  SSABRE DB Backup Script"
    echo "  시작 시간: $(date '+%Y-%m-%d %H:%M:%S')"
    echo "======================================"
    echo ""
    
    check_container
    create_backup_dir
    perform_backup
    compress_backup
    transfer_remote
    cleanup_old_backups
    
    echo ""
    show_backup_list
    
    echo ""
    echo "======================================"
    echo "  백업 완료!"
    echo "  종료 시간: $(date '+%Y-%m-%d %H:%M:%S')"
    echo "======================================"
}

# 스크립트 시작
main

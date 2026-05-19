#!/bin/bash

###############################################################################
# SSABRE 프로젝트 배포 스크립트 (Linux 서버용)
# 
# 사용법:
#   ./deploy.sh [OPTIONS]
#
# 옵션:
#   -h, --help           도움말 표시
#   -b, --backup         배포 전 DB 백업 수행
#   -p, --pull           Git pull 수행
#   -r, --rebuild        Docker 이미지 재빌드
#
# 예시:
#   ./deploy.sh                    # 기본 배포
#   ./deploy.sh -b                 # DB 백업 후 배포
#   ./deploy.sh -b -p -r           # 전체 업데이트 (백업 + pull + 재빌드)
###############################################################################

set -e  # 에러 발생 시 스크립트 중단

# 설정
PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
BACKUP_SCRIPT="$PROJECT_DIR/scripts/backup-db.sh"
DO_BACKUP=false
DO_PULL=false
DO_REBUILD=false

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 도움말 함수
show_help() {
    echo "SSABRE 프로젝트 배포 스크립트"
    echo ""
    echo "사용법: $0 [OPTIONS]"
    echo ""
    echo "옵션:"
    echo "  -h, --help           도움말 표시"
    echo "  -b, --backup         배포 전 DB 백업 수행"
    echo "  -p, --pull           Git pull 수행"
    echo "  -r, --rebuild        Docker 이미지 재빌드"
    echo ""
    echo "예시:"
    echo "  $0                    # 기본 배포"
    echo "  $0 -b                 # DB 백업 후 배포"
    echo "  $0 -b -p -r           # 전체 업데이트"
    exit 0
}

# 인자 파싱
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            ;;
        -b|--backup)
            DO_BACKUP=true
            shift
            ;;
        -p|--pull)
            DO_PULL=true
            shift
            ;;
        -r|--rebuild)
            DO_REBUILD=true
            shift
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

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# 환경 확인
check_environment() {
    log_step "환경 확인 중..."
    
    # Docker 확인
    if ! command -v docker &> /dev/null; then
        log_error "Docker가 설치되어 있지 않습니다."
        exit 1
    fi
    
    # Docker Compose 확인
    if ! command -v docker compose &> /dev/null; then
        log_error "Docker Compose가 설치되어 있지 않습니다."
        exit 1
    fi
    
    # .env 파일 확인
    if [ ! -f "$PROJECT_DIR/.env" ]; then
        log_error ".env 파일이 없습니다. 먼저 .env 파일을 생성하세요."
        log_info "참고: 실행가이드.md의 '환경 설정' 섹션을 확인하세요."
        exit 1
    fi
    
    log_info "환경 확인 완료"
}

# Git Pull
git_pull() {
    if [ "$DO_PULL" = true ]; then
        log_step "Git Pull 수행 중..."
        cd "$PROJECT_DIR"
        
        # 현재 브랜치 확인
        CURRENT_BRANCH=$(git branch --show-current)
        log_info "현재 브랜치: $CURRENT_BRANCH"
        
        # Pull 실행
        if git pull origin "$CURRENT_BRANCH"; then
            log_info "Git Pull 완료"
        else
            log_error "Git Pull 실패!"
            exit 1
        fi
    fi
}

# DB 백업
backup_database() {
    if [ "$DO_BACKUP" = true ]; then
        log_step "데이터베이스 백업 중..."
        
        if [ -f "$BACKUP_SCRIPT" ]; then
            chmod +x "$BACKUP_SCRIPT"
            bash "$BACKUP_SCRIPT" -c
        else
            log_warn "백업 스크립트를 찾을 수 없습니다: $BACKUP_SCRIPT"
            log_warn "수동으로 백업을 수행하세요."
        fi
    fi
}

# Docker 이미지 빌드
build_images() {
    if [ "$DO_REBUILD" = true ]; then
        log_step "Docker 이미지 재빌드 중..."
        cd "$PROJECT_DIR"
        
        # docker-compose.yml의 build 섹션 주석 해제 필요
        if docker compose build --no-cache; then
            log_info "이미지 빌드 완료"
        else
            log_error "이미지 빌드 실패!"
            exit 1
        fi
    fi
}

# 서비스 재시작
restart_services() {
    log_step "서비스 재시작 중..."
    cd "$PROJECT_DIR"
    
    # 컨테이너 중지
    log_info "컨테이너 중지 중..."
    docker compose stop
    
    # 컨테이너 시작
    log_info "컨테이너 시작 중..."
    if docker compose up -d; then
        log_info "서비스 시작 완료"
    else
        log_error "서비스 시작 실패!"
        exit 1
    fi
}

# 서비스 상태 확인
check_services() {
    log_step "서비스 상태 확인 중..."
    
    sleep 5  # 서비스가 시작될 때까지 대기
    
    # 실행 중인 컨테이너 확인
    log_info "실행 중인 컨테이너:"
    docker compose ps
    
    echo ""
    
    # Backend 헬스체크
    log_info "Backend 헬스체크 (최대 60초 대기)..."
    for i in {1..12}; do
        if docker exec ssabre_backend wget --quiet --tries=1 --spider http://localhost:8080/actuator/health 2>/dev/null; then
            log_info "Backend 정상 작동 중 ✓"
            break
        else
            if [ $i -eq 12 ]; then
                log_warn "Backend 헬스체크 시간 초과. 로그를 확인하세요."
            else
                echo -n "."
                sleep 5
            fi
        fi
    done
    
    echo ""
}

# 로그 표시
show_logs() {
    log_step "최근 로그 확인 (Ctrl+C로 종료)..."
    sleep 2
    docker compose logs --tail=50 -f
}

# 배포 정보 표시
show_deployment_info() {
    echo ""
    echo "======================================"
    echo "  배포 완료!"
    echo "======================================"
    echo ""
    log_info "서비스 URL:"
    echo "  - 프론트엔드: http://localhost 또는 http://your-server-ip"
    echo "  - 백엔드 API: http://localhost/api 또는 http://your-server-ip/api"
    echo "  - Grafana: http://localhost:3001 또는 http://your-server-ip/grafana"
    echo "  - Prometheus: http://your-server-ip/prometheus"
    echo "  - NetData: http://your-server-ip/netdata"
    echo ""
    log_info "유용한 명령어:"
    echo "  - 로그 확인: docker compose logs -f"
    echo "  - 서비스 재시작: docker compose restart <service-name>"
    echo "  - 서비스 중지: docker compose stop"
    echo "  - 컨테이너 상태: docker compose ps"
    echo ""
}

# 메인 실행
main() {
    echo "======================================"
    echo "  SSABRE 배포 스크립트"
    echo "  시작 시간: $(date '+%Y-%m-%d %H:%M:%S')"
    echo "======================================"
    echo ""
    
    check_environment
    git_pull
    backup_database
    build_images
    restart_services
    check_services
    
    show_deployment_info
    
    # 로그 표시 여부 확인
    read -p "$(echo -e ${BLUE}실시간 로그를 확인하시겠습니까? [y/N]: ${NC})" -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        show_logs
    fi
    
    echo ""
    echo "======================================"
    echo "  배포 완료!"
    echo "  종료 시간: $(date '+%Y-%m-%d %H:%M:%S')"
    echo "======================================"
}

# 스크립트 시작
main

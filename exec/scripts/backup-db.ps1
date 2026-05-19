# Windows용 DB 백업 스크립트
# PowerShell에서 실행

param(
    [switch]$Compress,
    [switch]$Help
)

# 설정
$CONTAINER_NAME = "ssabre_postgres"
$DB_NAME = "ssabre_db"
$DB_USER = "ssabre_user"
$BACKUP_DIR = "$env:USERPROFILE\ssabre_backups"
$TIMESTAMP = Get-Date -Format "yyyyMMdd_HHmmss"
$BACKUP_FILE = "ssabre_db_$TIMESTAMP.sql"

# 도움말
if ($Help) {
    Write-Host "SSABRE PostgreSQL Database Backup Script (Windows)" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "사용법: .\backup-db.ps1 [-Compress] [-Help]"
    Write-Host ""
    Write-Host "옵션:"
    Write-Host "  -Compress    백업 파일을 ZIP으로 압축"
    Write-Host "  -Help        도움말 표시"
    Write-Host ""
    Write-Host "예시:"
    Write-Host "  .\backup-db.ps1              # 기본 백업"
    Write-Host "  .\backup-db.ps1 -Compress    # 압축 백업"
    exit 0
}

# 로그 함수
function Log-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Green
}

function Log-Warn {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

function Log-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# 컨테이너 확인
function Check-Container {
    $containerRunning = docker ps --filter "name=$CONTAINER_NAME" --format "{{.Names}}"
    
    if (-not $containerRunning) {
        Log-Error "PostgreSQL 컨테이너 '$CONTAINER_NAME'가 실행 중이 아닙니다."
        Log-Info "다음 명령어로 컨테이너를 시작하세요: docker compose up -d postgres"
        exit 1
    }
}

# 백업 디렉토리 생성
function Create-BackupDir {
    if (-not (Test-Path -Path $BACKUP_DIR)) {
        Log-Info "백업 디렉토리 생성: $BACKUP_DIR"
        New-Item -ItemType Directory -Path $BACKUP_DIR -Force | Out-Null
    }
}

# DB 백업 실행
function Perform-Backup {
    Log-Info "데이터베이스 백업 시작..."
    Log-Info "컨테이너: $CONTAINER_NAME"
    Log-Info "데이터베이스: $DB_NAME"
    Log-Info "사용자: $DB_USER"
    
    $backupPath = Join-Path $BACKUP_DIR $BACKUP_FILE
    
    try {
        docker exec $CONTAINER_NAME pg_dump -U $DB_USER $DB_NAME | Out-File -FilePath $backupPath -Encoding UTF8
        Log-Info "백업 완료: $backupPath"
        
        # 파일 크기 확인
        $fileSize = (Get-Item $backupPath).Length / 1MB
        Log-Info "백업 파일 크기: $([Math]::Round($fileSize, 2)) MB"
        
        return $backupPath
    }
    catch {
        Log-Error "백업 실패: $_"
        exit 1
    }
}

# 백업 파일 압축
function Compress-Backup {
    param([string]$FilePath)
    
    if ($Compress) {
        Log-Info "백업 파일 압축 중..."
        
        $zipPath = "$FilePath.zip"
        
        try {
            Compress-Archive -Path $FilePath -DestinationPath $zipPath -Force
            Log-Info "압축 완료: $zipPath"
            
            # 압축 후 파일 크기
            $zipSize = (Get-Item $zipPath).Length / 1MB
            Log-Info "압축 파일 크기: $([Math]::Round($zipSize, 2)) MB"
            
            # 원본 파일 삭제
            Remove-Item $FilePath -Force
            Log-Info "원본 SQL 파일 삭제됨"
        }
        catch {
            Log-Error "압축 실패: $_"
            exit 1
        }
    }
}

# 오래된 백업 파일 정리 (30일 이상)
function Cleanup-OldBackups {
    Log-Info "30일 이상 된 백업 파일 정리 중..."
    
    $cutoffDate = (Get-Date).AddDays(-30)
    $oldFiles = Get-ChildItem -Path $BACKUP_DIR -Filter "ssabre_db_*" | Where-Object { $_.LastWriteTime -lt $cutoffDate }
    
    if ($oldFiles.Count -gt 0) {
        $oldFiles | Remove-Item -Force
        Log-Info "삭제된 백업 파일: $($oldFiles.Count) 개"
    }
    else {
        Log-Info "삭제할 오래된 백업 파일이 없습니다."
    }
}

# 백업 목록 표시
function Show-BackupList {
    Log-Info "최근 백업 파일 목록 (최신 5개):"
    Get-ChildItem -Path $BACKUP_DIR -Filter "ssabre_db_*" | 
        Sort-Object LastWriteTime -Descending | 
        Select-Object -First 5 | 
        ForEach-Object {
            $size = [Math]::Round($_.Length / 1MB, 2)
            Write-Host "  $($_.Name) ($size MB, $($_.LastWriteTime))"
        }
}

# 메인 실행
function Main {
    Write-Host "======================================" -ForegroundColor Cyan
    Write-Host "  SSABRE DB Backup Script (Windows)" -ForegroundColor Cyan
    Write-Host "  시작 시간: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
    Write-Host "======================================" -ForegroundColor Cyan
    Write-Host ""
    
    Check-Container
    Create-BackupDir
    $backupPath = Perform-Backup
    Compress-Backup -FilePath $backupPath
    Cleanup-OldBackups
    
    Write-Host ""
    Show-BackupList
    
    Write-Host ""
    Write-Host "======================================" -ForegroundColor Cyan
    Write-Host "  백업 완료!" -ForegroundColor Cyan
    Write-Host "  종료 시간: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
    Write-Host "======================================" -ForegroundColor Cyan
}

# 스크립트 시작
Main

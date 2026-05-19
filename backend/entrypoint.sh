#!/bin/sh

# 업로드 디렉토리 권한 설정 (호스트 볼륨 마운트 시 권한 문제 해결)
mkdir -p /app/uploads
chown -R spring:spring /app/uploads

# spring 사용자로 애플리케이션 실행 (PATH 환경변수 전달)
exec su spring -s /bin/sh -c "export PATH=$PATH; exec $*"

#!/bin/bash
set -e

APP_DIR="/home/ubuntu/app"
PM2="/usr/bin/pm2"
NODE="/usr/bin/node"
NPM="/usr/bin/npm"

cd "$APP_DIR"

# 환경 확인 로그
$NODE -v
$NPM -v
$PM2 -v

# 기존 프로세스 종료
$PM2 delete front || true

# Next.js 실행 (package.json의 start 스크립트 사용)
$PM2 start npm --name "front" -- start

exit 0

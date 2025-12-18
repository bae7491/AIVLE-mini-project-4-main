#!/bin/bash
set -e

export PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

APP_DIR="/home/ubuntu/app"
cd "$APP_DIR"

node -v
npm -v
pm2 -v

pm2 delete front || true
pm2 start npm --name "front" -- start
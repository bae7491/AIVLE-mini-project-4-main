#!/bin/bash
set -e

APP_DIR="/home/ubuntu/app"
PM2="/usr/bin/pm2"
NODE="/usr/bin/node"
NPM="/usr/bin/npm"

cd "$APP_DIR"

$NODE -v
$NPM -v
$PM2 -v

$PM2 delete front || true
$PM2 start npm --name "front" -- start
#!/bin/bash
set -e

APP_DIR="/home/ubuntu/app"

mkdir -p "$APP_DIR"
chown -R ubuntu:ubuntu "$APP_DIR"

cd "$APP_DIR"

npm ci
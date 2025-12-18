#!/bin/bash
set -e

APP_DIR="/home/ubuntu/app"
NPM="/usr/bin/npm"

cd "$APP_DIR"

$NPM ci

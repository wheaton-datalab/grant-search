#!/usr/bin/env bash
# run.sh — load .env then start Spring Boot

# 1) export all vars from .env
set -a
if [ -f .env ]; then
  source .env
fi
set +a

# 2) launch the app
mvn spring-boot:run "$@"

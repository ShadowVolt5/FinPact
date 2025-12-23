#!/usr/bin/env bash
#docker compose up -d --build

# чистый запуск
docker compose down -v
docker compose up -d --build

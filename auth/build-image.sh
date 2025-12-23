#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"

./gradlew clean buildFatJar --no-daemon
docker build -f Dockerfile -t finpact-auth:local ..

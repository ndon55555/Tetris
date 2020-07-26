#!/usr/bin/env bash

set -euo pipefail

./gradlew browser:dependencies --write-locks
./gradlew core:dependencies --write-locks
./gradlew desktop:dependencies --write-locks
#!/usr/bin/env bash

sudo chown -R "$USER":"$USER" "$GRADLE_HOME"
rm -rf "$GRADLE_HOME"/caches/*/plugin-resolution/
ls -l "$GRADLE_HOME"/caches/ \
  | grep --only-matching --perl-regexp "\d+\.\d+\.\d+\S*$" \
  | xargs --replace rm -rf "$GRADLE_HOME"/caches/{}
find "$GRADLE_HOME"/ -name "*.lock" -print0 | xargs -I {} rm -f {}
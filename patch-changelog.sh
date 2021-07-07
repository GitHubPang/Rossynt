#!/usr/bin/env bash

PLUGIN_VERSION=$(grep -E '^pluginVersion\s*=\s*.*$' plugin/gradle.properties)
PLUGIN_VERSION=${PLUGIN_VERSION##* }
echo Plugin version is \'${PLUGIN_VERSION}\'.
if [ -z "${PLUGIN_VERSION}" ]; then
  exit 1
fi

sed -i -E "s/\[Unreleased\]/\[Unreleased\]\n## \[${PLUGIN_VERSION}\] - $(date --iso-8601)/g" CHANGELOG.md || exit $?

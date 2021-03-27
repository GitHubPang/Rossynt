#!/usr/bin/env bash

TARGET_PATH=../plugin/src/main/resources/raw/RoslynSyntaxTreeBackend
IMAGE_NAME=temp-publish-roslyn-syntax-tree-backend
CONTAINER_NAME=temp-publish-roslyn-syntax-tree-backend-container

# Clean target path.
rm -rf "${TARGET_PATH}"

# Build in Docker.
docker build --tag "${IMAGE_NAME}" . || exit $?

# Copy artifacts to target path.
docker container rm "${CONTAINER_NAME}"
docker container create --name "${CONTAINER_NAME}" "${IMAGE_NAME}" || exit $?
docker container cp "${CONTAINER_NAME}":/app/ "${TARGET_PATH}" || exit $?
docker container rm "${CONTAINER_NAME}" || exit $?

# Delete the executables (we only need the DLLs).
rm --verbose "${TARGET_PATH}"/*/RoslynSyntaxTreeBackend

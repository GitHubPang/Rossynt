# Build Rossynt

Steps to build Rossynt:

1. [Build Backend](#build-backend)
2. [Build Plugin](#build-plugin)

## Build Backend

#### System Requirements

* Docker

#### Steps

Run in shell script:

```shell
cd backend
./publish.sh
```

The output will be in `plugin/src/main/resources/raw/RossyntBackend`.

## Build Plugin

#### System Requirements

* Java SDK

#### Steps

Run in shell script:

```shell
cd plugin
./gradlew buildPlugin
```

The output will be in `plugin/build/distributions`.

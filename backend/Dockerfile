# Reference: https://github.com/dotnet/dotnet-docker/blob/main/samples/aspnetapp/Dockerfile

# https://hub.docker.com/_/microsoft-dotnet
# Old ASP.NET Core runtimes are for unit test.
# https://stackoverflow.com/a/65485962
FROM mcr.microsoft.com/dotnet/aspnet:6.0-buster-slim AS build
COPY --from=mcr.microsoft.com/dotnet/sdk:7.0 /usr/share/dotnet /usr/share/dotnet

# Copy csproj and restore as distinct layers.
WORKDIR /source
COPY *.sln .
COPY RossyntBackendUnitTest/*.csproj ./RossyntBackendUnitTest/
COPY RossyntBackendIntegrationTest/*.csproj ./RossyntBackendIntegrationTest/
COPY RossyntBackend/*.csproj ./RossyntBackend/
RUN dotnet restore

# Copy everything else.
WORKDIR /source
COPY RossyntBackendUnitTest/. ./RossyntBackendUnitTest/
COPY RossyntBackendIntegrationTest/. ./RossyntBackendIntegrationTest/
COPY RossyntBackend/. ./RossyntBackend/

# Run unit test.
WORKDIR /source/RossyntBackendUnitTest
RUN dotnet test -c release --framework net7.0 -o /app-unit-test/net7.0
RUN dotnet test -c release --framework net6.0 -o /app-unit-test/net6.0

# Run integration test.
WORKDIR /source/RossyntBackendIntegrationTest
RUN dotnet test -c release --framework net7.0 -o /app-integration-test/net7.0
RUN dotnet test -c release --framework net6.0 -o /app-integration-test/net6.0

# Build app.
WORKDIR /source/RossyntBackend
RUN dotnet publish -c release --framework net7.0 -o /app/net7.0 --no-restore
RUN dotnet publish -c release --framework net6.0 -o /app/net6.0 --no-restore

# Reference: https://github.com/dotnet/dotnet-docker/blob/main/samples/aspnetapp/Dockerfile

# https://hub.docker.com/_/microsoft-dotnet
FROM mcr.microsoft.com/dotnet/sdk:5.0 AS build
WORKDIR /source

# copy csproj and restore as distinct layers
COPY *.sln .
COPY RoslynSyntaxTreeBackend/*.csproj ./RoslynSyntaxTreeBackend/
RUN dotnet restore

# copy everything else and build app
COPY RoslynSyntaxTreeBackend/. ./RoslynSyntaxTreeBackend/
WORKDIR /source/RoslynSyntaxTreeBackend
RUN dotnet publish -c release --framework net5.0 -o /app/net5.0 --no-restore
RUN dotnet publish -c release --framework netcoreapp3.1 -o /app/netcoreapp3.1 --no-restore
RUN dotnet publish -c release --framework netcoreapp2.1 -o /app/netcoreapp2.1 --no-restore

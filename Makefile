.PHONY: help generate-api build test run run-local run-debug-local clean install local-setup liquibase-update liquibase-rollback

help:
	@echo "Available commands:"
	@echo "  make generate-open-spec     - Generate DTOs and controller interfaces from OpenAPI spec"
	@echo "  make build             - Build the project"
	@echo "  make test              - Run tests"
	@echo "  make run               - Run the application"
	@echo "  make run-local         - Run the application with local profile"
	@echo "  make run-debug-local   - Run the application in debug mode with local profile"
	@echo "  make clean             - Clean build artifacts"
	@echo "  make install           - Install dependencies"
	@echo "  make local-setup       - Start local dependencies with Docker Compose"
	@echo "  make liquibase-update  - Apply all pending Liquibase changelogs"
	@echo "  make liquibase-rollback [COUNT=n] - Roll back last n Liquibase changeSets (default 1)"
	@echo "  make all               - Clean, install, generate, and build"

generate-open-spec:
	@echo "Generating DTOs and controller interfaces from OpenAPI spec..."
	./mvnw clean generate-sources

build:
	@echo "Building project..."
	./mvnw clean package -DskipTests

test:
	@echo "Running tests..."
	./mvnw test

run:
	@echo "Starting application..."
	./mvnw spring-boot:run

run-local:
	@echo "Starting application with local profile..."
	./mvnw spring-boot:run -Dspring-boot.run.profiles=local

run-debug-local:
	@echo "Starting application in debug mode with local profile..."
	./mvnw spring-boot:run -Dspring-boot.run.profiles=local -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"

clean:
	@echo "Cleaning build artifacts..."
	./mvnw clean

install:
	@echo "Installing dependencies..."
	./mvnw install -DskipTests

local-setup:
	@echo "Starting local setup (Postgres + Redis)..."
	docker compose -f docker-compose.yaml up

liquibase-update:
	@echo "Applying Liquibase changelogs..."
	./mvnw liquibase:update

liquibase-rollback:
	@echo "Rolling back the last ${COUNT:-1} Liquibase changesets..."
	./mvnw liquibase:rollback -Dliquibase.rollbackCount=$${COUNT:-1}

all: clean install generate-api build
	@echo "Build complete!"

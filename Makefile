.PHONY: help generate-api build test run clean install

help:
	@echo "Available commands:"
	@echo "  make generate-api    - Generate DTOs and controller interfaces from OpenAPI spec"
	@echo "  make build           - Build the project"
	@echo "  make test            - Run tests"
	@echo "  make run             - Run the application"
	@echo "  make clean           - Clean build artifacts"
	@echo "  make install         - Install dependencies"
	@echo "  make all             - Clean, install, generate, and build"

generate-api:
	@echo "Generating DTOs and controller interfaces from OpenAPI spec..."
	./mvnw clean generate-sources

build: generate-api
	@echo "Building project..."
	./mvnw clean package -DskipTests

test:
	@echo "Running tests..."
	./mvnw test

run:
	@echo "Starting application..."
	./mvnw spring-boot:run

clean:
	@echo "Cleaning build artifacts..."
	./mvnw clean

install:
	@echo "Installing dependencies..."
	./mvnw install -DskipTests

all: clean install generate-api build
	@echo "Build complete!"

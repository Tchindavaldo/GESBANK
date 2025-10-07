#!/bin/bash

# ============================================================================
# Bank Management System - Quick Start Script
# ============================================================================
# This script helps you quickly start the Bank Management System
# ============================================================================

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to print colored messages
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

print_header() {
    echo -e "${CYAN}"
    echo "╔══════════════════════════════════════════════════════════════════╗"
    echo "║                                                                  ║"
    echo "║     🏦  BANK MANAGEMENT SYSTEM - QUICK START                    ║"
    echo "║                                                                  ║"
    echo "╚══════════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check prerequisites
check_prerequisites() {
    print_info "Checking prerequisites..."

    local missing_deps=()

    if ! command_exists docker; then
        missing_deps+=("Docker")
    else
        print_success "Docker is installed"
    fi

    if ! command_exists docker-compose; then
        missing_deps+=("Docker Compose")
    else
        print_success "Docker Compose is installed"
    fi

    if ! command_exists java; then
        print_warning "Java is not installed (required for Maven mode)"
    else
        print_success "Java is installed ($(java -version 2>&1 | head -n 1))"
    fi

    if ! command_exists mvn; then
        print_warning "Maven is not installed (required for Maven mode)"
    else
        print_success "Maven is installed ($(mvn -version 2>&1 | head -n 1))"
    fi

    if [ ${#missing_deps[@]} -ne 0 ]; then
        print_error "Missing required dependencies: ${missing_deps[*]}"
        print_info "Please install the missing dependencies and try again."
        exit 1
    fi

    echo ""
}

# Function to start with Docker Compose
start_docker() {
    print_header
    print_info "Starting Bank Management System with Docker Compose..."
    echo ""

    # Check if .env file exists
    if [ ! -f .env ]; then
        print_warning ".env file not found. Using default configuration from docker-compose.yml"
    fi

    # Build and start containers
    print_info "Building Docker images (this may take a few minutes on first run)..."
    docker-compose build

    print_info "Starting services..."
    docker-compose up -d

    echo ""
    print_success "Services started successfully!"
    echo ""

    # Wait for application to be ready
    print_info "Waiting for the application to be ready..."
    local max_attempts=30
    local attempt=0

    while [ $attempt -lt $max_attempts ]; do
        if curl -s http://localhost:8080/api/auth/status > /dev/null 2>&1; then
            print_success "Application is ready!"
            break
        fi
        attempt=$((attempt + 1))
        echo -n "."
        sleep 2
    done

    if [ $attempt -eq $max_attempts ]; then
        print_warning "Application is taking longer than expected to start."
        print_info "Check logs with: docker-compose logs -f bank-app"
    fi

    echo ""
    print_success "Bank Management System is running!"
    echo ""
    print_info "Access the application:"
    echo "  📚 Swagger UI:    http://localhost:8080/swagger-ui.html"
    echo "  📖 API Docs:      http://localhost:8080/api-docs"
    echo "  🔐 Auth:          POST http://localhost:8080/api/auth/login"
    echo "  🗄️  pgAdmin:       http://localhost:5050 (if started with --admin)"
    echo ""
    print_info "Useful commands:"
    echo "  View logs:        docker-compose logs -f"
    echo "  Stop services:    docker-compose down"
    echo "  Restart:          docker-compose restart"
    echo ""
}

# Function to start with Docker and pgAdmin
start_docker_admin() {
    print_header
    print_info "Starting Bank Management System with Docker Compose (including pgAdmin)..."
    echo ""

    docker-compose --profile admin up -d --build

    echo ""
    print_success "Services started successfully!"
    echo ""
    print_info "Access the application:"
    echo "  📚 Swagger UI:    http://localhost:8080/swagger-ui.html"
    echo "  🗄️  pgAdmin:       http://localhost:5050"
    echo "     Email:         admin@banksystem.com"
    echo "     Password:      admin123"
    echo ""
}

# Function to start with Maven
start_maven() {
    print_header
    print_info "Starting Bank Management System with Maven..."
    echo ""

    # Check if PostgreSQL is running
    if ! pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
        print_warning "PostgreSQL is not running on localhost:5432"
        print_info "Starting PostgreSQL with Docker..."
        docker-compose up -d postgres

        print_info "Waiting for PostgreSQL to be ready..."
        sleep 5
    else
        print_success "PostgreSQL is running"
    fi

    # Compile and run
    print_info "Compiling the project..."
    mvn clean install -DskipTests

    print_info "Starting the application..."
    mvn spring-boot:run
}

# Function to stop services
stop_services() {
    print_header
    print_info "Stopping Bank Management System..."
    echo ""

    if [ -f docker-compose.yml ]; then
        docker-compose down
        print_success "Docker services stopped"
    fi

    # Kill any running Spring Boot process
    if pgrep -f "spring-boot:run" > /dev/null; then
        print_info "Stopping Maven Spring Boot process..."
        pkill -f "spring-boot:run"
        print_success "Maven process stopped"
    fi

    echo ""
    print_success "All services stopped"
}

# Function to show logs
show_logs() {
    print_header
    print_info "Showing application logs (press Ctrl+C to exit)..."
    echo ""

    if [ "$(docker ps -q -f name=bank-app)" ]; then
        docker-compose logs -f bank-app
    else
        print_error "Application container is not running"
        print_info "Start the application first with: ./start.sh"
    fi
}

# Function to display usage
show_usage() {
    print_header
    echo "Usage: ./start.sh [OPTION]"
    echo ""
    echo "Options:"
    echo "  (no option)    Start with Docker Compose (default)"
    echo "  --docker       Start with Docker Compose"
    echo "  --admin        Start with Docker Compose + pgAdmin"
    echo "  --maven        Start with Maven (requires PostgreSQL)"
    echo "  --stop         Stop all services"
    echo "  --logs         Show application logs"
    echo "  --status       Show status of services"
    echo "  --restart      Restart services"
    echo "  --clean        Stop services and remove volumes"
    echo "  --help         Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./start.sh                 # Start with Docker"
    echo "  ./start.sh --admin         # Start with pgAdmin"
    echo "  ./start.sh --maven         # Start with Maven"
    echo "  ./start.sh --logs          # View logs"
    echo ""
}

# Function to show status
show_status() {
    print_header
    print_info "Checking service status..."
    echo ""

    if [ "$(docker ps -q -f name=bank-postgres)" ]; then
        print_success "PostgreSQL: Running"
    else
        print_error "PostgreSQL: Not running"
    fi

    if [ "$(docker ps -q -f name=bank-app)" ]; then
        print_success "Application: Running"
        if curl -s http://localhost:8080/api/auth/status > /dev/null 2>&1; then
            print_success "API: Accessible at http://localhost:8080"
        else
            print_warning "API: Container running but not responding"
        fi
    else
        print_error "Application: Not running"
    fi

    if [ "$(docker ps -q -f name=bank-pgadmin)" ]; then
        print_success "pgAdmin: Running at http://localhost:5050"
    else
        print_info "pgAdmin: Not running (use --admin to start)"
    fi

    echo ""
}

# Function to restart services
restart_services() {
    print_header
    print_info "Restarting services..."
    echo ""

    docker-compose restart

    print_success "Services restarted"
    echo ""
    show_status
}

# Function to clean everything
clean_all() {
    print_header
    print_warning "This will stop all services and remove volumes (data will be lost)"
    read -p "Are you sure? (y/N): " -n 1 -r
    echo ""

    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_info "Stopping services and removing volumes..."
        docker-compose down -v
        print_success "All services stopped and volumes removed"
    else
        print_info "Cancelled"
    fi
}

# Main script
main() {
    # Check if we're in the right directory
    if [ ! -f "pom.xml" ] && [ ! -f "docker-compose.yml" ]; then
        print_error "This script must be run from the project root directory"
        exit 1
    fi

    # Make script executable if not already
    chmod +x "$0" 2>/dev/null || true

    case "${1:-}" in
        --docker)
            check_prerequisites
            start_docker
            ;;
        --admin)
            check_prerequisites
            start_docker_admin
            ;;
        --maven)
            start_maven
            ;;
        --stop)
            stop_services
            ;;
        --logs)
            show_logs
            ;;
        --status)
            show_status
            ;;
        --restart)
            restart_services
            ;;
        --clean)
            clean_all
            ;;
        --help|-h)
            show_usage
            ;;
        "")
            check_prerequisites
            start_docker
            ;;
        *)
            print_error "Unknown option: $1"
            echo ""
            show_usage
            exit 1
            ;;
    esac
}

# Run main function
main "$@"

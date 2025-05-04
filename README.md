# Roomily

Roomily is a comprehensive platform for room rental management and search. It connects landlords with potential tenants, providing tools for room listing, searching, budget planning, and secure transactions.

## Features

- **Authentication & Authorization**: Secure user registration and login system with role-based permissions
- **Room Management**: Allow landlords to create, update, and manage room listings
- **Room Search**: Advanced search functionality with filters for prospective tenants
- **Budget Planning**: Tools to help users plan their accommodation budget
- **Advertisement System**: Campaign management for promoting room listings
- **Payment Integration**: Secure payment processing using PayOS
- **User Reporting System**: Report malicious users or listings
- **Admin Dashboard**: Comprehensive system management for administrators
- **Real-time Notifications**: Using Firebase for instant notifications
- **Contract Generation**: Digital room rental contract creation

## Technology Stack

- **Backend**: Spring Boot with Java 21
- **Database**: PostgreSQL
- **Caching**: Redis
- **Storage**: MinIO for file storage
- **Messaging**: RabbitMQ for asynchronous messaging
- **Security**: Spring Security with JWT authentication
- **Documentation**: SpringDoc OpenAPI for API documentation
- **Push Notifications**: Firebase Cloud Messaging
- **Containerization**: Docker and Docker Compose

## Prerequisites

- Java 21 or higher
- Docker and Docker Compose
- Maven

## Getting Started

### Using Docker Compose

1. Clone the repository
2. Navigate to the project directory
3. Run the following command to start all required services:

```bash
docker-compose up -d
```

This will start PostgreSQL, Redis, MinIO, and RabbitMQ services.

### Running the Application

1. Build the application:

```bash
mvn clean package
```

2. Run the application:

```bash
java -jar target/Roomily-0.0.1-SNAPSHOT.jar
```

Or use Maven to run:

```bash
mvn spring-boot:run
```

## API Documentation

Once the application is running, you can access the API documentation at:

```
http://localhost:8080/swagger-ui.html
```

## Configuration

The application uses profiles for different environments. The default profile is 'dev'.

### Database Configuration

PostgreSQL database connection is configured in application.yml:

- Database: roomily
- Username: Configure in application.yml or environment variables
- Password: Configure in application.yml or environment variables

### External Services

- **MinIO**: Used for file storage
- **Redis**: Used for caching and session management
- **RabbitMQ**: Used for asynchronous messaging
- **Firebase**: Used for push notifications

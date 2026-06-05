# BillBuddy Backend

BillBuddy is a full-stack expense sharing application inspired by Splitwise, designed to help users manage personal and shared expenses, track balances, and settle debts within groups.

This repository contains the backend application built with Spring Boot.

---

## Features

### Authentication & Security

- JWT-based authentication and authorization
- Secure user registration and login

### User Management

- User profile management
- User search functionality
- Profile image upload via Cloudinary

### Expense Management

- Personal expense tracking
- Shared group expenses
- Expense categorization
- Multi-user expense splitting
- Expense creation and deletion

### Group Management

- Group creation and management
- Member invitations
- Member promotion and demotion
- Role system (Owner, Admin, Member)
- Group image upload via Cloudinary
- Debt-aware member removal restrictions

### Balance Engine

- Real-time balance calculation
- Group balance summaries
- Global balance summaries
- Debt tracking between members
- Settlement management
- Optimized payment suggestions

### API Infrastructure

- RESTful API architecture
- Request validation
- Centralized exception handling
- Pagination support

---

## Tech Stack

### Backend

- Java 25
- Spring Boot 4
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL
- JWT
- Cloudinary
- Maven
- Lombok

---


## Environment Configuration

Create an `env.properties` file in the project root and configure the following variables:

| Variable | Description |
|-----------|-------------|
| PORT | Application port |
| DB_PORT | PostgreSQL port |
| DB_NAME | Database name |
| DB_USERNAME | Database username |
| DB_PASSWORD | Database password |
| JWT_SECRET | Secret key used for JWT generation |
| FE_URL | Frontend application URL |
| CLOUDINARY_NAME | Cloudinary cloud name |
| CLOUDINARY_API_KEY | Cloudinary API key |
| CLOUDINARY_API_SECRET | Cloudinary API secret |

---

## Frontend Repository

The frontend application is available in a separate repository:

[BillBuddy Frontend](https://github.com/grazianinicola2000-dotcom/BillBuddy-FE)

---

## Key Features

- Personal and shared expense management
- Debt tracking and settlements
- Group collaboration and role management
- Real-time balance calculations
- Profile and group image uploads
- Secure JWT authentication

---

## Status

Current Version: **v1.0.0**

Initial stable release prepared for project delivery and demonstration.

---

## Author

Developed by **Nicola Graziani** as a full-stack capstone project.

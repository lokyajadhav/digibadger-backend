# Badger Backend (Spring Boot)

## Project Structure

```
badger/
└── src/
    └── main/
        └── java/
            └── com/
                └── badger/
                    ├── BadgerApplication.java
                    ├── controllers/      # REST controllers (API endpoints)
                    ├── services/         # Service interfaces
                    ├── serviceimpl/      # Service implementations
                    ├── repositories/     # Spring Data JPA repositories
                    ├── models/           # Entity and DTO classes
                    ├── configs/          # Security, CORS, and other configuration classes
                    ├── exceptions/       # Custom exceptions and handlers
                    ├── utils/            # Utility/helper classes
                    └── (other as needed)
        └── resources/
            ├── application.properties
            └── (other config files)
```

## Description
- **controllers/**: REST API endpoints
- **services/**: Service interfaces for business logic
- **serviceimpl/**: Implementations of service interfaces
- **repositories/**: Data access layer (Spring Data JPA)
- **models/**: Entities, DTOs, and data models
- **configs/**: Application and security configuration
- **exceptions/**: Custom exceptions and global exception handlers
- **utils/**: Utility and helper classes

Use this structure for all new features and modules. 
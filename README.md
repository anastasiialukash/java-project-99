[![Actions Status](https://github.com/anastasiialukash/java-project-99/actions/workflows/hexlet-check.yml/badge.svg)](https://github.com/anastasiialukash/java-project-99/actions)

### Hexlet tests and linter status:

https://java-project-99-yuuo.onrender.com/

## Application Configuration Profiles

This application supports multiple configuration profiles:

### Development Profile (dev)

The development profile is optimized for local development with features like:
- In-memory H2 database with console access
- Detailed SQL logging and formatting
- Enhanced debug logging
- Automatic schema updates

To run the application with the development profile:
```bash
./gradlew bootRun
# or explicitly
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Production Profile (prod)

The production profile is optimized for deployment with features like:
- Environment variable configuration for database connections
- Disabled development tools (H2 console)
- Minimal logging
- Performance optimizations
- No automatic schema updates

To run the application with the production profile:
```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
# or set environment variable
export SPRING_PROFILES_ACTIVE=prod
./gradlew bootRun
```

### Test Profile

A separate test profile is configured for running tests with:
- In-memory H2 database
- Create-drop schema generation
- Appropriate test logging levels

Tests automatically use this profile via the `@ActiveProfiles("test")` annotation.
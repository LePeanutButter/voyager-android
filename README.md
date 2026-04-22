# SmarTrip - Kotlin-Based Android App Architecture

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.20-blue.svg)
![Gradle](https://img.shields.io/badge/Gradle-8.2-green.svg)
![Android](https://img.shields.io/badge/Android-API%2024%2B-brightgreen.svg)
![License](https://img.shields.io/badge/License-GPL%203.0-blue.svg)
![Platform](https://img.shields.io/badge/Platform-Android-lightgrey.svg)

> A complete Android application scaffolding for a Tourism Intelligent Platform built with Kotlin, following Clean Architecture principles and modern Android development practices.

## Table of Contents

- [Background](#background)
- [Install](#install)
- [Usage](#usage)
- [Architecture](#architecture)
- [Features](#features)
- [API](#api)
- [Configuration](#configuration)
- [Contributing](#contributing)
- [License](#license)

## Background

Voyager Tourism is a comprehensive Android application template that demonstrates best practices for building scalable, maintainable mobile applications using modern Android development stack. The project showcases Clean Architecture principles, MVVM pattern, and industry-standard libraries for dependency injection, networking, and local storage.

Key architectural decisions:
- **Clean Architecture**: Separation of concerns with distinct layers (Presentation, Domain, Data)
- **MVVM Pattern**: Model-View-ViewModel for UI state management
- **Dependency Injection**: Hilt for compile-time DI
- **Reactive Programming**: Kotlin Coroutines and Flow for async operations
- **Modern UI**: Jetpack Compose with Material Design 3

## Install

### Prerequisites

- **Android Studio**: Hedgehog | 2023.1.1 or later
- **JDK**: Version 8 or higher
- **Android SDK**: API level 24+ (Android 7.0)
- **Gradle**: Version 8.2 or higher

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-org/voyager-android.git
   cd voyager-android
   ```

2. **Open in Android Studio**
   ```bash
   # Alternatively, open Android Studio and select "Open an existing project"
   # Navigate to the cloned directory
   ```

3. **Configure API endpoints**
   ```kotlin
   // Update in app/src/main/java/com/voyager/tourism/di/NetworkModule.kt
   .baseUrl("https://api.your-tourism-platform.com/v1/")
   ```

4. **Build the project**
   ```bash
   ./gradlew build
   ```

5. **Run the application**
   ```bash
   ./gradlew installDebug
   # Or run directly from Android Studio
   ```

### Gradle Commands

```bash
# Build debug version
./gradlew assembleDebug

# Build release version
./gradlew assembleRelease

# Run tests
./gradlew test

# Run instrumentation tests
./gradlew connectedAndroidTest

# Clean build
./gradlew clean
```

## Usage

### Running the Application

1. **Launch from Android Studio**
   - Select the app module
   - Choose a connected device or emulator
   - Click the Run button

2. **Command Line**
   ```bash
   ./gradlew installDebug && adb shell am start -n com.voyager.tourism/.presentation.ui.splash.SplashActivity
   ```

### Key Features Usage

#### Authentication
```kotlin
// Login example
viewModel.login("user@example.com", "password123")

// Registration example
viewModel.register(
    email = "user@example.com",
    password = "password123",
    username = "traveler",
    firstName = "John",
    lastName = "Doe"
)
```

#### Trip Management
```kotlin
// Create a new trip
val trip = Trip(
    id = UUID.randomUUID().toString(),
    userId = "current-user-id",
    title = "Paris Adventure",
    description = "Weekend trip to Paris",
    destination = Destination(/* ... */),
    startDate = System.currentTimeMillis(),
    endDate = System.currentTimeMillis() + 86400000,
    budget = 1500.0,
    travelers = 2
)

viewModel.createTrip(trip)
```

#### Navigation
```kotlin
// Navigate between screens
navController.navigate("dashboard")
navController.navigate("trip_detail/$tripId")
navController.navigate("recommendations")
```

## Architecture

### Clean Architecture Overview

```
app/src/main/java/com/voyager/tourism/
```

#### Presentation Layer (`presentation/`)
- **UI Components**: Jetpack Compose screens and UI elements
- **ViewModels**: State management using AndroidX ViewModel
- **Navigation**: Jetpack Navigation Compose setup

#### Domain Layer (`domain/`)
- **Entities**: Core business models (User, Trip, Destination)
- **Use Cases**: Application business logic encapsulation
- **Repository Interfaces**: Data access contracts

#### Data Layer (`data/`)
- **Repository Implementations**: Concrete data access logic
- **Data Sources**: Remote (Retrofit) and local (Room) sources
- **DTOs**: Data transfer objects for API communication

### Technology Stack

- **Language**: Kotlin 1.9.20
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Hilt 2.48
- **Networking**: Retrofit 2.9.0 + OkHttp 4.12.0
- **Database**: Room 2.6.1
- **Async**: Kotlin Coroutines 1.7.3
- **Navigation**: Navigation Compose 2.7.5

### Project Structure

```
app/
src/main/java/com/voyager/tourism/
|-- data/
|   |-- api/                    # Retrofit API services
|   |-- database/              # Room database setup
|   |   |-- dao/               # Data access objects
|   |   |-- entity/            # Database entities
|   |   |-- converter/         # Type converters
|   |-- dto/                   # Data transfer objects
|   |-- local/                 # SharedPreferences management
|   |-- mapper/                # Domain/Data model mappers
|   |-- repository/            # Repository implementations
|-- domain/
|   |-- model/                 # Domain entities
|   |-- repository/            # Repository interfaces
|   |-- usecase/               # Business logic use cases
|   |-- |-- auth/              # Authentication use cases
|   |-- |-- trip/              # Trip management use cases
|   |-- |-- recommendation/    # AI recommendation use cases
|-- presentation/
|   |-- MainActivity.kt        # Main activity entry point
|   |-- viewmodel/             # ViewModels for state management
|   |-- ui/                    # Compose screens by feature
|   |   |-- auth/              # Authentication screens
|   |   |-- dashboard/         # Main dashboard
|   |   |-- trip/              # Trip management screens
|   |   |-- profile/           # User profile screen
|   |   |-- recommendations/   # AI recommendations
|   |   |-- splash/            # Splash screen
|   |-- navigation/            # Navigation setup
|-- di/                        # Hilt dependency injection modules
|   |-- DatabaseModule.kt      # Database dependencies
|   |-- NetworkModule.kt       # Network dependencies
|   |-- RepositoryModule.kt    # Repository bindings
TourismApplication.kt          # Application class with @HiltAndroidApp
```

## Features

### Core Features

- **AI-Powered Travel Assistant**: Personalized recommendations and trip planning
- **User Authentication**: Secure login/registration with token management
- **Trip Management**: Complete CRUD operations for travel itineraries
- **Destination Discovery**: AI-powered destination and activity recommendations
- **Social Features**: Connect with fellow travelers
- **Offline Support**: Local database caching with Room
- **Modern UI**: Material Design 3 with Jetpack Compose

### Technical Features

- **Clean Architecture**: Scalable and maintainable codebase
- **MVVM Pattern**: Proper separation of UI and business logic
- **Dependency Injection**: Compile-time DI with Hilt
- **Reactive Programming**: Coroutines and Flow for async operations
- **Local Storage**: Room database for offline support
- **Network Layer**: Retrofit with OkHttp for API communication
- **State Management**: ViewModels with StateFlow and Compose state

## API

### Authentication Endpoints

```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

```http
POST /auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "username": "traveler",
  "first_name": "John",
  "last_name": "Doe"
}
```

### Trip Management Endpoints

```http
GET /trips?userId={userId}
Authorization: Bearer {token}
```

```http
POST /trips
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Paris Adventure",
  "description": "Weekend trip to Paris",
  "destination": { ... },
  "start_date": 1640995200000,
  "end_date": 1641081600000,
  "budget": 1500.0,
  "travelers": 2
}
```

### AI Recommendations

```http
GET /recommendations/destinations?userId={userId}
Authorization: Bearer {token}
```

```http
POST /ai/chat
Authorization: Bearer {token}
Content-Type: application/json

{
  "message": "What are the best places to visit in Paris?",
  "context": "trip_planning"
}
```

## Configuration

### Environment Configuration

Create a `local.properties` file in the project root:

```properties
# API Configuration
API_BASE_URL=https://api.your-tourism-platform.com/v1/
API_KEY=your_api_key_here

# Build Configuration
DEBUG_MODE=true
LOG_LEVEL=DEBUG
```

### Build Variants

- **Debug**: Development build with logging and debugging enabled
- **Release**: Production build with optimizations and obfuscation

### Gradle Configuration

Key configurations in `app/build.gradle.kts`:

```kotlin
android {
    compileSdk = 34
    defaultConfig {
        applicationId = "com.voyager.tourism"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
}
```

## Contributing

We welcome contributions! Please follow these guidelines:

### Development Setup

1. **Fork the repository**
   ```bash
   git clone https://github.com/LePeanutButter/voyager-android.git
   cd voyager-android
   ```

2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make your changes**
   - Follow Kotlin coding conventions
   - Add tests for new functionality
   - Update documentation as needed

4. **Run tests**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

5. **Submit a pull request**
   - Provide clear description of changes
   - Include relevant test coverage
   - Ensure CI checks pass

### Code Style Guidelines

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add comprehensive comments for complex logic
- Keep functions small and focused (single responsibility)
- Use dependency injection consistently

### Architecture Rules

- **Domain Layer**: No Android framework dependencies
- **Data Layer**: Can depend on Android frameworks (Room, Retrofit)
- **Presentation Layer**: Only depends on Domain Layer
- **Dependency Injection**: Use Hilt for all dependencies

### Testing Strategy

- **Unit Tests**: Test business logic in use cases
- **Integration Tests**: Test repository implementations
- **UI Tests**: Test user interactions with Compose
- **Test Coverage**: Maintain >80% coverage for critical components

## License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file for details.

## License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file for details.

### License Summary

- **Commercial Use**: Yes
- **Modification**: Yes
- **Distribution**: Yes
- **Private Use**: Yes
- **Liability**: No
- **Warranty**: No

### Copyright

© 2024 Voyager Team. All rights reserved.
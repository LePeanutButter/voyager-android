# SmarTrip - Kotlin-Based Android App Architecture

[![Standard Readme](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=flat-square)](https://github.com/RichardLitt/standard-readme)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-blue.svg)
![Gradle](https://img.shields.io/badge/Gradle-9.5-green.svg)
![AGP](https://img.shields.io/badge/AGP-8.13.2-green.svg)
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
- [Testing](#testing)
- [Contributors](#contributors)
- [License](#license)

## Background

**SmarTrip** (módulo Android del ecosistema Voyager) es una aplicación que sigue buenas prácticas de Android moderno: arquitectura limpia, MVVM, inyección de dependencias (Hilt), red (Retrofit/Moshi), almacenamiento local y UI con Jetpack Compose, alineada con el cliente web `voyager-web-client`.

Key architectural decisions:
- **Clean Architecture**: Separation of concerns with distinct layers (Presentation, Domain, Data)
- **MVVM Pattern**: Model-View-ViewModel for UI state management
- **Dependency Injection**: Hilt for compile-time DI
- **Reactive Programming**: Kotlin Coroutines and Flow for async operations
- **Modern UI**: Jetpack Compose with Material Design 3

## Install

### Prerequisites

- **Android Studio**: Latest stable (recommended) with Kotlin 2.x and Compose support
- **JDK**: 17 (matches `compileOptions` / `kotlinOptions` in the app module)
- **Android SDK**: API level 24+ (Android 7.0)
- **Gradle**: 9.5+ (via the included wrapper)

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/LePeanutButter/voyager-android.git
   cd voyager-android
   ```

2. **Open in Android Studio**
   ```bash
   # Alternatively, open Android Studio and select "Open an existing project"
   # Navigate to the cloned directory
   ```

3. **Configure API endpoints**
   - Set base URLs in `gradle.properties` (project root):
     - `VOYAGER_BACKEND_BASE_URL` — Spring Boot API (must end with `/api/v1/` or the build will normalize it)
     - `VOYAGER_AI_BASE_URL` — FastAPI AI service (must end with `/api/v1/` when applicable)
   - Values are injected at build time as `BuildConfig.BACKEND_BASE_URL` and `BuildConfig.AI_SERVICE_BASE_URL` and wired in `NetworkModule`.

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

# Unit tests (JVM)
./gradlew :app:testDebugUnitTest

# Unit test coverage (Android Gradle Plugin report)
./gradlew :app:createDebugUnitTestCoverageReport

# JaCoCo HTML/XML (custom task; excludes UI/DI/generated noise — see app/build.gradle.kts)
./gradlew :app:jacocoTestReport

# Run instrumentation tests (device/emulator)
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
- **Interceptors**: JWT attachment (`AuthInterceptor`), session reset on `401` (`UnauthorizedResponseInterceptor`)
- **Session**: `SessionInvalidationNotifier` for global sign-out flows

### Technology Stack

- **Language**: Kotlin 2.0.20
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Hilt 2.51
- **Networking**: Retrofit 2.9.0 + OkHttp 4.12.0 + Moshi 1.15.0
- **Database**: Room 2.6.1
- **Async**: Kotlin Coroutines 1.7.3
- **Navigation**: Navigation Compose 2.7.5
- **Quality**: JaCoCo (unit test coverage), JUnit 4, MockK, Robolectric (local prefs), AndroidX Test

### Project Structure

```
app/
src/main/java/com/voyager/tourism/
|-- data/
|   |-- api/                    # Retrofit API services
|   |-- database/               # Room database, entities, DAOs, Converters.kt
|   |-- dto/                    # Data transfer objects
|   |-- interceptor/            # OkHttp interceptors (auth, unauthorized)
|   |-- local/                  # PreferencesManager, TokenManager
|   |-- mapper/                 # Domain/Data model mappers
|   |-- repository/             # Repository implementations
|   |-- session/                # Session invalidation signals (e.g. SharedFlow)
|-- domain/
|   |-- model/                  # Domain entities
|   |-- repository/             # Repository interfaces
|   |-- usecase/                # Business logic use cases
|   |   |-- auth/
|   |   |-- trip/
|   |   |-- behavior/
|   |   |-- recommendation/
|   |   |-- social/
|-- presentation/
|   |-- MainActivity.kt
|   |-- viewmodel/              # ViewModels
|   |-- ui/                     # Compose screens by feature
|   |   |-- auth/
|   |   |-- assistant/
|   |   |-- dashboard/
|   |   |-- trip/
|   |   |-- place/
|   |   |-- profile/
|   |   |-- recommendations/
|   |   |-- social/
|   |   |-- splash/
|   |-- navigation/
|-- di/                         # Hilt modules (Database, Network, Repository)
TourismApplication.kt           # @HiltAndroidApp
```

## Features

### Core Features

- **AI-Powered Travel Assistant**: Personalized recommendations and trip planning
- **User Authentication**: Secure login/registration, Google OAuth flow, token and session handling
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

Prefer **`gradle.properties`** at the project root for service URLs consumed by the Android build:

```properties
# Backend (Spring Boot) — typically .../api/v1/
VOYAGER_BACKEND_BASE_URL=https://your-host:8080/api/v1/

# AI microservice (FastAPI) — typically .../api/v1/
VOYAGER_AI_BASE_URL=https://your-host:8000/api/v1/

# Optional broker URL for traveler chat real-time transport
VOYAGER_CHAT_BROKER_URL=wss://your-host/ws-chat
```

Optional `local.properties` (not committed) can hold SDK paths and machine-specific overrides; keep secrets out of version control.

### Build Variants

- **Debug**: Development build with logging, debugging, and **unit test coverage** instrumentation enabled (`enableUnitTestCoverage`)
- **Release**: Production build with optimizations (minify currently off by default — adjust as needed)

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
        // BACKEND_BASE_URL / AI_SERVICE_BASE_URL from gradle.properties
    }
}
```

## Testing

- **Unit tests** live under `app/src/test/java` and cover use cases, mappers, Room type converters, OkHttp interceptors, local session helpers, and ViewModels (with MockK and coroutine-friendly dispatchers).
- **Robolectric** is used where Android `Context` is required (e.g. `SharedPreferences`-backed managers).
- **Coverage**
  - `./gradlew :app:createDebugUnitTestCoverageReport` — full-module report under `app/build/reports/coverage/test/debug/`
  - `./gradlew :app:jacocoTestReport` — JaCoCo report with package exclusions for Compose UI, `di`, and generated/Hilt glue (see `jacocoTestReport` in `app/build.gradle.kts`)

Overall coverage percentage is dominated by Compose UI and untested integration paths; interpret reports by package when gating quality on domain/data logic.

## Contributors

- Andrés Felipe Calderón Ramírez - [AndresFelipeCalderonRamirez](https://github.com/AndresFelipeCalderonRamirez)
- Laura Natalia Perilla Quintero - [Lanapequin](https://github.com/Lanapequin)
- Ricardo Andres Ayala Garzon - [lRicardol](https://github.com/lRicardol)
- Santiago Amaya Zapata - [SantiagoAmaya21](https://github.com/SantiagoAmaya21)
- Santiago Botero Garcia - [LePeanutButter](https://github.com/LePeanutButter)

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

© 2026 SmarTrip / Voyager. All rights reserved.

# Nammaskills

Nammaskills is a modern Android application designed for skill center management and course discovery. It provides a platform for users to find skill centers, explore courses, and manage their enrollments, while also providing administrative features for center management.

## 🚀 Features

- **Course Discovery:** Browse through a wide range of skill-based courses.
- **Skill Center Locator:** Find and view skill centers on an interactive map.
- **Role-Based Access:** Distinct experiences for regular users and administrators.
- **Real-time Updates:** Powered by Firebase for real-time data synchronization.
- **Offline Support:** Local caching using Room database for a seamless experience.
- **Success Stories:** Explore stories from successful candidates.

## 🛠️ Tech Stack

- **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) for a modern, declarative UI.
- **Dependency Injection:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) for robust and scalable DI.
- **Database:** [Room](https://developer.android.com/training/data-storage/room) for local data persistence.
- **Networking:** [Retrofit](https://square.github.io/retrofit/) & [Gson](https://github.com/google/gson) for API communication.
- **Backend/Cloud:** [Firebase](https://firebase.google.com/) (Auth, Realtime Database, Storage, Firestore).
- **Maps:** [MapLibre](https://maplibre.org/) for interactive map features.
- **Image Loading:** [Coil](https://coil-kt.github.io/coil/compose/) for efficient image loading.
- **Navigation:** [Jetpack Navigation](https://developer.android.com/guide/navigation) for Compose.

## 🏗️ Project Structure

The project follows clean architecture principles:
- `data`: Repositories, DAOs, and remote data sources.
- `domain`: Models and business logic interfaces.
- `ui`: Composable screens, themes, and ViewModels.
- `di`: Dependency injection modules.
- `util`: Helper classes and utilities.

## 🏁 Getting Started

1. **Clone the repository:**
   ```bash
   git clone https://github.com/YOUR_USERNAME/Nammaskills.git
   ```
2. **Setup Firebase:**
   - Add your `google-services.json` to the `app/` directory.
3. **Build the project:**
   - Open in Android Studio and sync Gradle files.
   - Run the `:app` module on an emulator or physical device.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

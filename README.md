# TripBuddy Mobile Application

TripBuddy is a mobile application designed to help users plan and manage their trips more efficiently.  
The app focuses on organizing travel details and improving the overall travel planning experience.

## Project Type
- Mobile Application
- Student / Academic Project

## Features
- Trip planning and management
- User-friendly interface
- Organized travel information
- Simple and intuitive navigation

## Technologies Used
- Java (Android)
- Android Studio
- XML (UI Design)

## How to Run the Project
1. Open the project in Android Studio
2. Allow Gradle to finish syncing
3. Run the application on an emulator or physical Android device

## Purpose of the Project
This project was created to:
- Apply mobile app development concepts
- Practice Android application structure
- Improve problem-solving and design skills
- Gain hands-on experience with real-world app ideas

## What I Learned
- Android application lifecycle
- UI design using XML
- Event handling and navigation
- Structuring a mobile application
- Team collaboration (if applicable)

## Future Improvements
- Add user authentication
- Improve UI/UX design
- Integrate a backend or API
- Add more trip-related features

---



---

## Upgrade notes (database, security, performance, scalability, concurrency)

### Where files go
The upload was a flat folder, so files now sit in the standard Android layout. Existing files keep their names, class names, view IDs and navigation flow.
`app/src/main/java/com/example/tripbuddy/` (code), `app/src/main/res/` (layouts, strings, xml), `app/src/test/` (unit tests).
Copy `AndroidManifest.xml` changes and the dependencies in `app/build.gradle.additions.txt` into your project (the upload had no manifest or Gradle files, so merge rather than overwrite).

### Architecture
Screens (Activities) -> Repositories (`repo/`) -> DAOs (`data/`) -> SQLite (`TripBuddyDbHelper`). Screens never touch SQL.
- `core/` AppExecutors (thread pools), Money and BudgetCalculator (pricing in whole cents), Callback and AppException
- `security/` PasswordHasher, InputValidator, SessionManager
- `model/` immutable data classes, `ui/` list adapters, `ServiceLocator` shares one repository per type

### Database
SQLite with tables `users`, `trips`, `memories`; foreign keys with cascade delete, CHECK constraints, indexes for the history queries, WAL mode, versioned migrations in `onUpgrade`. Rows carry a `uuid` and `updated_at`, so a remote sync can be added later without changing the schema.

### Security
- Salted PBKDF2-HMAC-SHA256 password hashing (310,000 iterations, stored inside the hash string so it can be raised later); constant-time compare
- Sign-in lockout (5 wrong passwords = 5 minutes), atomic in SQL; same error and same timing for unknown users
- All SQL parameterised; every query scoped by `user_id`; prices and totals recomputed in the repository, never trusted from the UI
- Input validation and length limits; sessions expire after 7 days; manifest hardening (no backup, no cleartext, activities not exported); login redirect only to allow-listed screens

### Concurrency and performance
- No database or hashing work on the main thread: single writer thread, small reader pool, separate crypto pool
- Optimistic locking (`version` column) on trips; save + confirm in one transaction; double-tap protection
- Callbacks are dropped if a screen closed meanwhile; keyset pagination (20 per page) with DiffUtil lists; MediaPlayer released correctly

### Tests
`app/src/test` covers money, pricing rules, password hashing and validation (`./gradlew test`).

ScreenShots
<img width="1776" height="1009" alt="Screenshot (35)" src="https://github.com/user-attachments/assets/654d95dc-d4df-4253-b184-b6d4ddea3e5f" />
<img width="1812" height="1087" alt="Screenshot (36)" src="https://github.com/user-attachments/assets/fe7659b2-966f-4fce-9883-68ca961b8cac" />
<img width="1738" height="984" alt="Screenshot (34)" src="https://github.com/user-attachments/assets/041f56b6-4347-4348-9fea-7331dd13bba3" />


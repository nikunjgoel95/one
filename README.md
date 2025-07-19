# One - A Fasting Tracker for Android and Wear OS

One is a simple yet powerful fasting tracker for Android and Wear OS. It helps you track your fasting progress, set goals, and view your history.

## Project Structure

The project is divided into three modules:

*   **`app`**: The Android application. It provides the main user interface for interacting with the fasting tracker.
*   **`onewearos`**: The Wear OS application. It provides a watch face complication and a tile for a quick view of your fasting progress. It also has a standalone Wear OS app.
*   **`shared`**: A common library module that contains the core business logic, data models, and communication layer for both the Android and Wear OS applications. It uses the Wear OS Data Layer API to sync data between the two devices.

## Features

### Android App

*   Track your fasting progress with a beautiful and intuitive user interface.
*   Set fasting goals and get notified when you reach them.
*   View your fasting history and track your progress over time.
*   A home screen widget to see your progress at a glance.

### Wear OS App

*   A watch face complication that shows your current fasting progress.
*   A tile for a quick view of your fasting progress.
*   A standalone Wear OS app to view your progress and history.
*   Ongoing activity support to track your fast even when the app is not in the foreground.

## How it works

The `shared` module contains the core business logic of the application. It uses a Room database to store the fasting data. The `app` and `onewearos` modules use this shared module to access the data and display it to the user.

The `shared` module also contains a `FastingEventManager` that is responsible for managing the fasting state. The `app` and `onewearos` modules use this manager to start, stop, and update the fasting progress.

The `shared` module uses the Wear OS Data Layer API to sync the fasting data between the phone and the watch. This ensures that the data is always up-to-date on both devices.

## Building and Running

To build and run the project, you will need to have Android Studio installed.

1.  Clone the repository: `git clone https://github.com/your-username/one.git`
2.  Open the project in Android Studio.
3.  Create a `keystore.properties` file in the root of the project with the following content:

    ```
    storeFile=<path to your keystore file>
    storePassword=<your keystore password>
    keyAlias=<your key alias>
    keyPassword=<your key password>
    ```
4.  Sync the project with Gradle.
5.  Run the `app` module on an Android device or emulator.
6.  Run the `onewearos` module on a Wear OS device or emulator.

## Contributing

Contributions are welcome! If you have any ideas, suggestions, or bug reports, please open an issue or create a pull request.

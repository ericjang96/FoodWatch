# FoodWatch
This is an Android app that lets users quickly browse and search restaurant health inspection histories for my hometown: Surrey, BC. It uses data from the City of Surrey website, and I plan to add more cities depending on the available APIs.

### How to try the app (Installs a release version of the app):
1. Download the APK file from: https://github.com/ericjang96/FoodWatch/releases/download/0.1.0/app-release.apk
2. Move the file to a physical device or Android emulator with API level 25 (all levels 23+ should also work, but have not been thoroughly tested)
3. Find the file on your device and tap it to install the app (you may need to modify security settings to allow installation of apk files from unknown sources.)

### How to build this project (Installs a debug version of the app):
1. Pull to a local repository
2. **Important:** Use your own Google API key in FoodWatch/app/src/main/res/values/strings.xml on this line:  
\<string name="google_api_key">ENTER YOUR API KEY HERE\</string>  
You must have at least these two APIs enabled for the API key:
    * Google Maps Android API
    * Google Places API for Android
3. Open a command prompt in .../FoodWatch directory
4. Connect a physical debuggable device or start an Android emulator with API level 25 (all levels 23+ should also work, but have not been thoroughly tested)
5. Run the command "build-with-gradle.bat" to build the project. This script build the APK, install it on a connected device, run all unit + integration tests, and create a coverage report.  
**Coverage report location**: FoodWatch/app/build/reports/jacoco/jacocoTestReport/html/index.html  
**APK location**: FoodWatch/app/build/outputs/apk

You shouldn't need a local installation of Gradle as the wrapper will install it for you on your first "gradlew" command.

### Note:
There are differences between the release version and debug version of this app:
 1. Some messages are only logged on the debug build
 2. The list of inspections for restaurants in the debug build are ordered from oldest to newest (as opposed to newest to oldest in the release build). This was done so that integration tests can verify the inspection without dealing with NestedScrollView and Espresso issues. It was an issue to have the newest inspection at the top because the top element can be updated. The tests need the top element to be the same inspection item each time, so the oldest inspection is at the top now.

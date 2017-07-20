# FoodWatch
This is an Android app that lets users quickly browse and search restaurant health inspection histories for my hometown: Surrey, BC. It uses data from the City of Surrey website, and I plan to add more cities depending on the available APIs.

<div class="center-vid">
  <video class="border-vid" src="app_demo_1.mp4" width="360" height="640" controls preload></video>
<!--  <video class="border-vid" src="app_demo_1.mp4" width="360" height="640" controls preload></video> This is a placeholder for multiple videos in the future -->
</div>

### How to try the app (Installs a release version of the app):
1. Download the APK file from: https://github.com/ericjang96/FoodWatch/releases/download/0.1.0/app-release.apk
2. Move the file to a physical device or Android emulator with API level 23 or higher
3. Find the file on your device and tap it to install the app (you may need to modify security settings to allow installation of apk files from unknown sources.)

### How to build this project (Installs a debug version of the app):
1. Pull to a local repository
2. **Important:** Use your own Google API key in FoodWatch/app/src/main/res/values/strings.xml on this line:  
\<string name="google_api_key">ENTER YOUR API KEY HERE\</string>  
You must have at least these two APIs enabled for the API key:
    * Google Maps Android API
    * Google Places API for Android
3. Open a command prompt in .../FoodWatch directory
4. Connect a physical device or start an Android emulator with API level 23 or higher
5. Run the command "gradlew tasks" to see a list of tasks you can run (e.g. "gradlew installDebug" will install the app on the connected device. "gradlew createDebugCoverageReport" will run all automated tests and create a code coverage report.)

You shouldn't need a local installation of Gradle as the wrapper will install it for you on your first "gradlew" command.

SDK Manager Plugin
==================

SDK missing? API level not downloaded? Support library out-of-date?

These are all typical problems which you shouldn't have to deal with. This is especially painful
when you have multiple developers on a project or a CI machine that you have to keep up-to-date.

This Gradle plugin will manage these SDK dependencies for you automatically.

Supported functionality:

 * `local.properties` will be created if missing. The `ANDROID_HOME` environment variable will be
   used if present. Otherwise `~/.android-sdk` will be used.
 * The platform-specific SDK will be downloaded if missing.
 * The build tools and platform tools will be downloaded if missing.
 * Compilation API declared in `compileSdkVersion` will be downloaded if missing.
 * If any dependencies are declared on support libraries, the support repository will be downloaded
   if missing. If the revision of the support repository does not contain the version declared it
   will be updated.
 * If any dependencies are declared on Google Play Services, the Google repository will be
   downloaded if missing. If the revision of the Google repository does not contain the version
   declared it will be updated.
 * If an emulator is specified, it will be downloaded if missing. If the emulator revision is less
   than the available revision, it will be updated.


*Note: By using this plugin you acknowledge that associated licenses of the components downloaded
are accepted automatically on your behalf. You are required to have accepted the respective licenses
of these components prior to using this plugin.*



Usage
-----

Apply the plugin in your `build.gradle` *before* the regular 'android' plugin:
```groovy
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath 'com.android.tools.build:gradle:0.12.+'
    classpath 'com.jakewharton.sdkmanager:gradle-plugin:0.12.+'
  }
}

apply plugin: 'android-sdk-manager'
apply plugin: 'com.android.application'

// optionally including an emulator
sdkManager {
  emulatorVersion 'android-19'
  emulatorArchitecture 'armeabi-v7a' // optional, defaults to arm
}
```

On an initial run, the output will look something like this:
```
$ ./gradlew clean assemble
Android SDK not found. Downloading...
SDK extracted at '/Users/jw/.android-sdk'. Writing to local.properties.
Build tools 20.0.0 missing. Downloading...
Compilation API android-19 missing. Downloading...
Support library repository missing. Downloading...
Google Play Services repository missing. Downloading...
Emulator version not installed or outdated. Downloading...

(normal execution output)
```
Your output likely will be different depending on the varying factors listed above. Subsequent runs
will omit this output and proceed directly to normal execution.

How long does this plugin add to the build lifecycle? It currently takes about 100ms on average to
check all of the above conditions. This cost is only paid when Gradle is setting up the model for
the project. If you use the Gradle daemon or use Android Studio this only happens once.



License
--------

    Copyright 2014 Jake Wharton

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

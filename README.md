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
 * Compilation API declared in `compileSdkVersion` will be downloaded if missing.
 * If any dependencies are declared on support libraries, the support repository will be downloaded
   if missing. If the revision of the support repository does not contain the version declared it
   will be updated.
 * If any dependencies are declared on Google Play Services, the Google repository will be
   downloaded if missing. If the revision of the Google repository does not contain the version
   declared it will be updated.



Usage
-----

Apply the plugin in your `build.gradle` *before* the regular 'android' plugin:
```groovy
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath 'com.android.tools.build:gradle:0.9.+'
    classpath 'com.jakewharton.sdkmanager:gradle-plugin:0.9.+'
  }
}

apply plugin: 'android-sdk-manager'
apply plugin: 'android'
```



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

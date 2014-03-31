package com.jakewharton.sdkmanager.internal

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.StopExecutionException

import static com.android.SdkConstants.ANDROID_HOME_ENV
import static com.android.SdkConstants.FN_LOCAL_PROPERTIES
import static com.android.SdkConstants.SDK_DIR_PROPERTY

class SdkResolver {
  static File resolve(Project project) {
    return new SdkResolver(project, new System.Real(), new Downloader.Real()).resolve()
  }

  final Logger log = Logging.getLogger SdkResolver
  final Project project
  final System system
  final Downloader downloader
  final File userHome
  final File userAndroidTemp
  final File userAndroid
  final File localProperties

  SdkResolver(Project project, System system, Downloader downloader) {
    this.project = project
    this.system = system
    this.downloader = downloader

    userHome = new File(system.property('user.home'))
    userAndroidTemp = new File(userHome, '.android-sdk.temp')
    userAndroid = new File(userHome, '.android-sdk')

    localProperties = new File(project.rootDir, FN_LOCAL_PROPERTIES)
  }

  File resolve() {
    // Check for existing local.properties file and the SDK it points to.
    if (localProperties.exists()) {
      log.debug "Found $FN_LOCAL_PROPERTIES at '$localProperties.absolutePath'."
      def properties = new Properties()
      localProperties.withInputStream { properties.load it }
      def sdkDirPath = properties.getProperty SDK_DIR_PROPERTY
      log.debug "Found $SDK_DIR_PROPERTY of '$sdkDirPath'."
      def sdkDir = new File(sdkDirPath)
      if (!sdkDir.exists()) {
        throw new StopExecutionException(
            "Specified SDK directory '$sdkDirPath' in '$FN_LOCAL_PROPERTIES' is not found.")
      }
      return sdkDir
    }

    log.debug "Missing $FN_LOCAL_PROPERTIES."

    // Look for ANDROID_HOME environment variable.
    def androidHome = system.env ANDROID_HOME_ENV
    if (androidHome != null) {
      def sdkDir = new File(androidHome)
      if (!sdkDir.exists()) {
        throw new StopExecutionException(
            "Specified SDK directory '$androidHome' in '$ANDROID_HOME_ENV' is not found.")
      }

      log.debug "Found $ANDROID_HOME_ENV at '$androidHome'. Writing to $FN_LOCAL_PROPERTIES."

      writeLocalProperties androidHome
      return new File(androidHome)
    }

    log.debug "Missing $ANDROID_HOME_ENV."

    // Look for an SDK in the home directory.
    if (userAndroid.exists()) {
      log.debug "Found existing SDK at '$userAndroid.absolutePath'. Writing to $FN_LOCAL_PROPERTIES."

      writeLocalProperties userAndroid.absolutePath
      return userAndroid
    }

    log.lifecycle 'Android SDK not found. Downloading...'

    // Download the SDK zip and extract it.
    downloader.download(userAndroidTemp, userAndroid)
    log.lifecycle "SDK extracted at '$userAndroid.absolutePath'. Writing to $FN_LOCAL_PROPERTIES."

    writeLocalProperties userAndroid.absolutePath
    return userAndroid
  }

  def writeLocalProperties(String path) {
    localProperties.withOutputStream {
      it << "# DO NOT check this file into source control.\n"
      it << "$SDK_DIR_PROPERTY=$path\n"
    }
  }
}

package com.jakewharton.sdkmanager.internal

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.StopExecutionException

import static com.android.SdkConstants.ANDROID_HOME_ENV
import static com.android.SdkConstants.FN_LOCAL_PROPERTIES
import static com.android.SdkConstants.PLATFORM_WINDOWS
import static com.android.SdkConstants.SDK_DIR_PROPERTY
import static com.android.SdkConstants.currentPlatform

class SdkResolver {
  static File resolve(Project project) {
    boolean isWindows = currentPlatform() == PLATFORM_WINDOWS
    return new SdkResolver(project, new System.Real(), new Downloader.Real(), isWindows).resolve()
  }

  final Logger log = Logging.getLogger SdkResolver
  final Project project
  final System system
  final Downloader downloader
  final File userHome
  final File userAndroid
  final File localProperties
  final boolean isWindows

  SdkResolver(Project project, System system, Downloader downloader, boolean isWindows) {
    this.project = project
    this.system = system
    this.downloader = downloader
    this.isWindows = isWindows

    userHome = new File(system.property('user.home'))
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
      if (sdkDirPath != null) {
        log.debug "Found $SDK_DIR_PROPERTY of '$sdkDirPath'."
        def sdkDir = new File(sdkDirPath)
        if (!sdkDir.exists()) {
          throw new StopExecutionException(
              "Specified SDK directory '$sdkDirPath' in '$FN_LOCAL_PROPERTIES' is not found.")
        }
        return sdkDir
      }

      log.debug "Missing $SDK_DIR_PROPERTY in $FN_LOCAL_PROPERTIES."
    } else {
      log.debug "Missing $FN_LOCAL_PROPERTIES."
    }

    // Look for ANDROID_HOME environment variable.
    def androidHome = system.env ANDROID_HOME_ENV
    if (androidHome != null && !"".equals(androidHome)) {
      def sdkDir = new File(androidHome)
      if (sdkDir.exists()) {
        log.debug "Found $ANDROID_HOME_ENV at '$androidHome'. Writing to $FN_LOCAL_PROPERTIES."
        writeLocalProperties androidHome
      } else {
        log.debug "Found $ANDROID_HOME_ENV at '$androidHome' but directory is missing."
        downloadSdk sdkDir
      }
      return sdkDir
    }

    log.debug "Missing $ANDROID_HOME_ENV."

    // Look for an SDK in the home directory.
    if (userAndroid.exists()) {
      log.debug "Found existing SDK at '$userAndroid.absolutePath'. Writing to $FN_LOCAL_PROPERTIES."

      writeLocalProperties userAndroid.absolutePath
      return userAndroid
    }

    downloadSdk userAndroid
    return userAndroid
  }

  def downloadSdk(File target) {
    log.lifecycle 'Android SDK not found. Downloading...'

    // Download the SDK zip and extract it.
    downloader.download target
    log.lifecycle "SDK extracted at '$target.absolutePath'. Writing to $FN_LOCAL_PROPERTIES."

    writeLocalProperties target.absolutePath
  }

  def writeLocalProperties(String path) {
    if (isWindows) {
      // Escape Windows file separators when writing as a path.
      path = path.replace "\\", "\\\\"
    }
    if (localProperties.exists()) {
      localProperties.withWriterAppend('UTF-8') {
        it.write "$SDK_DIR_PROPERTY=$path\n" as String
      }
    } else {
      localProperties.withWriter('UTF-8') {
        it.write "# DO NOT check this file into source control.\n"
        it.write "$SDK_DIR_PROPERTY=$path\n" as String
      }
    }
  }
}

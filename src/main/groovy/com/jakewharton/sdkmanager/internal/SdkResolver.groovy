package com.jakewharton.sdkmanager.internal

import org.apache.log4j.Logger
import org.gradle.api.Project
import org.gradle.api.tasks.StopExecutionException

import static com.android.SdkConstants.ANDROID_HOME_ENV
import static com.android.SdkConstants.FN_LOCAL_PROPERTIES
import static com.android.SdkConstants.SDK_DIR_PROPERTY

class SdkResolver {
  static File resolve(Project project) {
    return new SdkResolver(project, new System.Real(), new Downloader.Real()).resolve()
  }

  final def log = Logger.getLogger SdkResolver
  final Project project
  final System system
  final Downloader downloader
  final File userHome
  final File userAndroidTemp
  final File userAndroid

  SdkResolver(Project project, System system, Downloader downloader) {
    this.project = project
    this.system = system
    this.downloader = downloader

    userHome = new File(system.property('user.home'))
    userAndroidTemp = new File(userHome, '.android-sdk.temp')
    userAndroid = new File(userHome, '.android-sdk')
  }

  File resolve() {
    def localProperties = project.file FN_LOCAL_PROPERTIES

    // Check for existing local.properties file and the SDK it points to.
    if (localProperties.exists()) {
      log.debug "Found local.properties at '$localProperties.absolutePath'."
      def properties = new Properties()
      localProperties.withInputStream { properties.load it }
      def sdkDirPath = properties.getProperty SDK_DIR_PROPERTY
      log.debug "Found sdk.dir at '$sdkDirPath'."
      def sdkDir = new File(sdkDirPath)
      if (!sdkDir.exists()) {
        // TODO test this case!!
        throw new StopExecutionException(
            "Specified SDK directory '$sdkDirPath' in 'local.properties' is not found.")
      }
      return sdkDir
    }

    log.debug 'Missing local.properties.'

    // Look for ANDROID_HOME environment variable.
    def androidHome = system.env ANDROID_HOME_ENV
    if (androidHome != null) {
      log.debug "Found ANDROID_HOME at '$androidHome'. Writing to local.properties."

      localProperties.withOutputStream {
        it << "$SDK_DIR_PROPERTY=$androidHome"
      }
      return new File(androidHome)
    }

    log.debug 'Missing ANDROID_HOME.'

    // Look for an SDK in the home directory.
    if (userAndroid.exists()) {
      log.debug "Found existing SDK at '$userAndroid.absolutePath'. Writing to local.properties."

      localProperties.withOutputStream {
        it << "$SDK_DIR_PROPERTY=$userAndroid.absolutePath"
      }

      return userAndroid
    }

    log.info 'Downloading and extracting Android SDK.'

    // Download the SDK zip and extract it.
    downloader.download(userAndroidTemp, userAndroid)
    log.debug "SDK extracted at '$userAndroid.absolutePath'. Writing to local.properties."

    localProperties.withOutputStream {
      it << "$SDK_DIR_PROPERTY=$userAndroid.absolutePath"
    }

    return userAndroid
  }
}

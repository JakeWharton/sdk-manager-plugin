package com.jakewharton.sdkmanager.internal

import org.apache.log4j.Logger
import org.gradle.api.Project
import org.gradle.api.tasks.StopExecutionException

class SdkResolver {
  final def log = Logger.getLogger SdkResolver
  final def userHome = new File(System.getProperty('user.home'))
  final def userAndroidTemp = new File(userHome, '.android-sdk.temp')
  final def userAndroid = new File(userHome, '.android-sdk')
  final def project

  SdkResolver(Project project) {
    this.project = project;
  }

  File resolve() {
    def localProperties = project.file 'local.properties'

    // Check for existing local.properties file and the SDK it points to.
    if (localProperties.exists()) {
      log.debug "Found local.properties at '$localProperties.absolutePath'."
      def properties = new Properties()
      localProperties.withInputStream { properties.load it }
      def sdkDirPath = properties.getProperty 'sdk.dir'
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
    def androidHome = SystemEnvironment.getValue 'ANDROID_HOME'
    if (androidHome != null) {
      log.debug "Found ANDROID_HOME at '$androidHome'. Writing to local.properties."

      localProperties.withOutputStream {
        it << "sdk.dir=$androidHome"
      }
      return new File(androidHome)
    }

    log.debug 'Missing ANDROID_HOME.'

    // Look for an SDK in the home directory.
    if (userAndroid.exists()) {
      log.debug "Found existing SDK at '$userAndroid.absolutePath'. Writing to local.properties."

      localProperties.withOutputStream {
        it << "sdk.dir=$userAndroid.absolutePath"
      }

      return userAndroid
    }

    log.info 'Downloading and extracting Android SDK.'

    // Download the SDK zip and extract it.
    SdkDownload.get().download(userAndroidTemp, userAndroid)
    log.debug "SDK extracted at '$userAndroid.absolutePath'. Writing to local.properties."

    localProperties.withOutputStream {
      it << "sdk.dir=$userAndroid.absolutePath"
    }

    return userAndroid
  }
}

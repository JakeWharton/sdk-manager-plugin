package com.jakewharton.sdkmanager

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.jakewharton.sdkmanager.internal.SdkResolver
import org.apache.log4j.Logger
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.StopExecutionException

class SdkManagerPlugin implements Plugin<Project> {
  final def log = Logger.getLogger SdkManagerPlugin

  @Override void apply(Project project) {
    def hasApp = project.plugins.hasPlugin AppPlugin
    def hasLib = project.plugins.hasPlugin LibraryPlugin
    if (hasApp || hasLib) {
      throw new StopExecutionException(
          "Must be applied before 'android' or 'android-library' plugin.")
    }

    File sdkFolder = new SdkResolver(project).resolve()
    log.debug "Using SDK folder $sdkFolder.absolutePath."

    project.afterEvaluate {
      ensureDependencies()
    }
  }

  def ensureDependencies() {
    // TODO
  }
}

package com.jakewharton.sdkmanager

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.jakewharton.sdkmanager.internal.PackageResolver
import com.jakewharton.sdkmanager.internal.SdkResolver
import org.apache.log4j.Logger
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.StopExecutionException
import java.util.concurrent.TimeUnit

class SdkManagerPlugin implements Plugin<Project> {
  def log = Logger.getLogger SdkManagerPlugin

  @Override void apply(Project project) {
    def hasApp = project.plugins.hasPlugin AppPlugin
    def hasLib = project.plugins.hasPlugin LibraryPlugin
    if (hasApp || hasLib) {
      throw new StopExecutionException(
          "Must be applied before 'android' or 'android-library' plugin.")
    }

    // Eager resolve the SDK and local.properties pointer.
    def sdk
    time "SDK resolve", {
      sdk = SdkResolver.resolve project
    }

    // Defer resolving SDK package dependencies until after the model is finalized.
    project.afterEvaluate {
      time "Package resolve", {
        PackageResolver.resolve project, sdk
      }
    }
  }

  def time(String name, Closure task) {
    long before = System.nanoTime()
    task.run()
    long after = System.nanoTime()
    long took = TimeUnit.NANOSECONDS.toMillis(after - before)
    log.debug "$name took $took ms."
  }
}

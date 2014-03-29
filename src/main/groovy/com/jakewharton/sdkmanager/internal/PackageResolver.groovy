package com.jakewharton.sdkmanager.internal

import com.android.sdklib.repository.FullRevision
import org.apache.log4j.Logger
import org.gradle.api.Project

class PackageResolver {
  static void resolve(Project project, File sdk) {
    new PackageResolver(project, sdk).resolve()
  }

  final Logger log = Logger.getLogger PackageResolver
  final Project project
  final File sdk
  final File buildToolsDir
  final File platformsDir

  PackageResolver(Project project, File sdk) {
    this.sdk = sdk
    this.project = project

    buildToolsDir = new File(sdk, 'build-tools')
    platformsDir = new File(sdk, 'platforms')
  }

  void resolve() {
    resolveBuildTools()
    resolveCompileVersion()
  }

  private void resolveBuildTools() {
    FullRevision buildToolsRevision = project.android.buildToolsRevision
    log.debug "Build tools version: $buildToolsRevision"

    def buildToolsRevisionDir = new File(buildToolsDir, buildToolsRevision.toString())
    if (!buildToolsRevisionDir.exists()) {
      log.debug "Build tools missing!!!"
    }
  }

  private void resolveCompileVersion() {
    String compileVersion = project.android.compileSdkVersion
    log.debug "Compile SDK version: $compileVersion"

    def compileVersionDir = new File(platformsDir, compileVersion)
    if (!compileVersionDir.exists()) {
      log.debug "Platform missing!!!"
    }
  }
}

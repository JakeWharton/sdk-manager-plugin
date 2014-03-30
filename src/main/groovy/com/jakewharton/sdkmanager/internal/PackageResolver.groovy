package com.jakewharton.sdkmanager.internal

import com.android.sdklib.repository.FullRevision
import org.apache.log4j.Logger
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency

class PackageResolver {
  static void resolve(Project project, File sdk) {
    new PackageResolver(project, sdk).resolve()
  }

  final Logger log = Logger.getLogger PackageResolver
  final Project project
  final File sdk
  final File buildToolsDir
  final File platformsDir
  final File androidRepositoryDir
  final File googleRepositoryDir
  final File androidExecutable

  PackageResolver(Project project, File sdk) {
    this.sdk = sdk
    this.project = project

    buildToolsDir = new File(sdk, 'build-tools')
    platformsDir = new File(sdk, 'platforms')

    def extrasDir = new File(sdk, 'extras')
    def androidExtrasDir = new File(extrasDir, 'android')
    androidRepositoryDir = new File(androidExtrasDir, 'm2repository')
    def googleExtrasDir = new File(extrasDir, 'google')
    googleRepositoryDir = new File(googleExtrasDir, 'm2repository')

    def toolsDir = new File(sdk, 'tools')
    def platform = Platform.get()
    androidExecutable = new File(toolsDir, platform.androidExecutable)
    androidExecutable.setExecutable true, false
  }

  def resolve() {
    resolveBuildTools()
    resolveCompileVersion()
    resolveSupportLibraryRepository()
    resolvePlayServiceRepository()
  }

  def resolveBuildTools() {
    FullRevision buildToolsRevision = project.android.buildToolsRevision
    log.debug "Build tools version: $buildToolsRevision"

    def buildToolsRevisionDir = new File(buildToolsDir, buildToolsRevision.toString())
    if (buildToolsRevisionDir.exists()) {
      log.debug 'Build tools found!'
      return
    }

    log.info "Build tools $buildToolsRevision missing. Downloading from SDK manager."

    def code = downloadPackage "build-tools-$buildToolsRevision"
    if (code != 0) {
      log.error "Build tools download failed with code $code."
    }
  }

  def resolveCompileVersion() {
    String compileVersion = project.android.compileSdkVersion
    log.debug "Compile API version: $compileVersion"

    def compileVersionDir = new File(platformsDir, compileVersion)
    if (compileVersionDir.exists()) {
      log.debug 'Compilation API found!'
      return
    }

    log.info "Compilation API $compileVersion missing. Downloading from SDK manager."

    def code = downloadPackage compileVersion
    if (code != 0) {
      log.error "Compilation API download failed with code $code."
    }
  }

  def resolveSupportLibraryRepository() {
    def supportLibraryDep = findPlayServicesDependency 'com.android.support'
    if (supportLibraryDep == null) {
      log.debug 'No support library dependency found.'
      return
    }

    log.debug "Found support library dependency: $supportLibraryDep"

    def needsDownload = false;
    if (!androidRepositoryDir.exists()) {
      needsDownload = true
      log.info 'Support library repository missing. Downloading from SDK manager.'
    } else {
      // TODO determine if we need to download
    }

    if (needsDownload) {
      downloadPackage 'extra-android-m2repository'
    }
  }

  def resolvePlayServiceRepository() {
    def playServicesDep = findPlayServicesDependency 'com.google.android.gms'
    if (playServicesDep == null) {
      log.debug 'No Play services dependency found.'
      return
    }

    log.debug "Found Play services dependency: $playServicesDep"

    def needsDownload = false;
    if (!googleRepositoryDir.exists()) {
      needsDownload = true
      log.info 'Play services repository missing. Downloading from SDK manager.'
    } else {
      // TODO determine if we need to download
    }

    if (needsDownload) {
      downloadPackage 'extra-google-m2repository'
    }
  }

  def findPlayServicesDependency(String group) {
    for (Configuration configuration : project.configurations) {
      for (Dependency dependency : configuration.dependencies) {
        if (group.equals(dependency.group)) {
          return dependency
        }
      }
    }
    return null
  }

  def downloadPackage(String filter) {
    // -a == all
    // -u == no UI
    // -t == filter
    def cmd = [androidExecutable.absolutePath, 'update', 'sdk', '-a', '-u', '-t', filter]
    def process = cmd.execute()

    // Press 'y' and then enter on the license prompt.
    def output = new OutputStreamWriter(process.out)
    output.write("y\n")
    output.close()

    // Pipe the command output to our log.
    def input = new InputStreamReader(process.in)
    def line
    while ((line = input.readLine()) != null) {
      log.debug line
    }

    return process.waitFor()
  }
}

package com.jakewharton.sdkmanager.internal

import com.android.SdkConstants
import com.android.sdklib.repository.FullRevision
import org.apache.commons.io.FileUtils
import org.apache.log4j.Logger
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency

import static com.android.SdkConstants.FD_BUILD_TOOLS
import static com.android.SdkConstants.FD_EXTRAS
import static com.android.SdkConstants.FD_M2_REPOSITORY
import static com.android.SdkConstants.FD_PLATFORMS
import static com.android.SdkConstants.FD_TOOLS

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

    buildToolsDir = new File(sdk, FD_BUILD_TOOLS)
    platformsDir = new File(sdk, FD_PLATFORMS)

    def extrasDir = new File(sdk, FD_EXTRAS)
    def androidExtrasDir = new File(extrasDir, 'android')
    androidRepositoryDir = new File(androidExtrasDir, FD_M2_REPOSITORY)
    def googleExtrasDir = new File(extrasDir, 'google')
    googleRepositoryDir = new File(googleExtrasDir, FD_M2_REPOSITORY)

    def toolsDir = new File(sdk, FD_TOOLS)
    androidExecutable = new File(toolsDir, SdkConstants.androidCmdName())
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

      // Add future repository to the project since the main plugin skips it when missing.
      androidRepositoryDir.mkdirs()
      project.repositories.maven {
        url = androidRepositoryDir
      }
    } else {
      def repoVersion = newestVersion androidRepositoryDir
      if (!fulfillsDependency(supportLibraryDep.version, repoVersion)) {
        needsDownload = true
        log.info 'Support library repository outdated. Downloading update from SDK manager.'
      }
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

      // Add future repository to the project since the main plugin skips it when missing.
      googleRepositoryDir.mkdirs()
      project.repositories.maven {
        url = googleRepositoryDir
      }
    } else {
      def repoVersion = newestVersion googleRepositoryDir
      if (!fulfillsDependency(playServicesDep.version, repoVersion)) {
        needsDownload = true
        log.info 'Play services repository outdated. Downloading update from SDK manager.'
      }
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

  static def newestVersion(File repository) {
    def pattern = /(\d+)\.(\d+)\.(\d+)/
    def maxMajor = -1
    def maxMinor = -1
    def maxPatch = -1
    for (File file : FileUtils.listFiles(repository, ['pom'] as String[], true)) {
      def match = ( file.name =~ pattern )
      def major = match[0][1] as int
      def minor = match[0][2] as int
      def patch = match[0][3] as int
      if (major > maxMajor) {
        maxMajor = major
        maxMinor = minor
        maxPatch = patch
      } else if (major == maxMajor && minor > maxMinor) {
        maxMinor = minor
        maxPatch = patch
      } else if (major == maxMinor && minor == maxMinor && patch > maxPatch) {
        maxPatch = patch
      }
    }
    return [maxMajor, maxMinor, maxPatch] as int[]
  }

  static def fulfillsDependency(String version, int[] repoVersion) {
    def match = ( version =~ /(\d*\+?)(?:\.(\d*\+?)(?:\.(\d*\+?))?)?/ )
    def major = match[0][1]
    if ('+'.equals(major)) {
      return true
    }
    if ((major as int) > repoVersion[0]) {
      return false
    }
    def minor = match[0][2]
    if (minor == null || '+'.equals(minor)) {
      return true
    }
    if ((minor as int) > repoVersion[1]) {
      return false
    }
    def patch = match[0][3]
    if (patch == null || '+'.equals(patch)) {
      return true
    }
    if ((patch as int) > repoVersion[2]) {
      return false
    }
    return true
  }
}

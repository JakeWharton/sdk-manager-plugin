package com.jakewharton.sdkmanager.internal

import com.android.sdklib.repository.FullRevision
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.StopExecutionException

import static com.android.SdkConstants.FD_BUILD_TOOLS
import static com.android.SdkConstants.FD_EXTRAS
import static com.android.SdkConstants.FD_M2_REPOSITORY
import static com.android.SdkConstants.FD_PLATFORMS
import static com.android.SdkConstants.FD_ADDONS
import static com.android.SdkConstants.FD_PLATFORM_TOOLS

class PackageResolver {
  static void resolve(Project project, File sdk) {
    new PackageResolver(project, sdk, new AndroidCommand.Real(sdk)).resolve()
  }

  static final String GOOGLE_API_PREFIX = "Google Inc.:Google APIs:"

  final Logger log = Logging.getLogger PackageResolver
  final Project project
  final File sdk
  final File buildToolsDir
  final File platformToolsDir
  final File platformsDir
  final File addonsDir
  final File androidRepositoryDir
  final File googleRepositoryDir
  final AndroidCommand androidCommand

  PackageResolver(Project project, File sdk, AndroidCommand androidCommand) {
    this.sdk = sdk
    this.project = project
    this.androidCommand = androidCommand

    buildToolsDir = new File(sdk, FD_BUILD_TOOLS)
    platformToolsDir = new File(sdk, FD_PLATFORM_TOOLS)
    platformsDir = new File(sdk, FD_PLATFORMS)
    addonsDir = new File(sdk, FD_ADDONS)

    def extrasDir = new File(sdk, FD_EXTRAS)
    def androidExtrasDir = new File(extrasDir, 'android')
    androidRepositoryDir = new File(androidExtrasDir, FD_M2_REPOSITORY)
    def googleExtrasDir = new File(extrasDir, 'google')
    googleRepositoryDir = new File(googleExtrasDir, FD_M2_REPOSITORY)
  }

  def resolve() {
    resolveBuildTools()
    resolvePlatformTools()
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

    log.lifecycle "Build tools $buildToolsRevision missing. Downloading..."

    def code = androidCommand.update "build-tools-$buildToolsRevision"
    if (code != 0) {
      throw new StopExecutionException("Build tools download failed with code $code.")
    }
  }

  def resolvePlatformTools() {
    if (platformToolsDir.exists()) {
      log.debug 'Platform tools found!'
      return
    }

    log.lifecycle "Platform tools missing. Downloading..."

    def code = androidCommand.update "platform-tools"
    if (code != 0) {
      throw new StopExecutionException("Platform tools download failed with code $code.")
    }
  }

  def resolveCompileVersion() {
    String compileVersion = project.android.compileSdkVersion
    log.debug "Compile API version: $compileVersion"

    if (compileVersion.startsWith(GOOGLE_API_PREFIX)) {
      // The google SDK requires the base android SDK as a prerequisite, but
      // the SDK manager won't follow dependencies automatically.
      def baseVersion = compileVersion.replace(GOOGLE_API_PREFIX, "android-")
      installIfMissing(platformsDir, baseVersion)
      def addonVersion = compileVersion.replace(GOOGLE_API_PREFIX, "addon-google_apis-google-")
      installIfMissing(addonsDir, addonVersion);
    } else {
      installIfMissing(platformsDir, compileVersion);
    }
  }

  def installIfMissing(baseDir, version) {
    def existingDir = new File(baseDir, version)
    if (existingDir.exists()) {
      log.debug "Compilation API $version found!"
      return
    }

    log.lifecycle "Compilation API $version missing. Downloading..."

    def code = androidCommand.update version
    if (code != 0) {
      throw new StopExecutionException("Compilation API $version download failed with code $code.")
    }
  }

  def resolveSupportLibraryRepository() {
    def supportLibraryDeps = findDependenciesWithGroup 'com.android.support'
    if (supportLibraryDeps.isEmpty()) {
      log.debug 'No support library dependency found.'
      return
    }

    log.debug "Found support library dependencies: $supportLibraryDeps"

    def needsDownload = false;
    if (!androidRepositoryDir.exists()) {
      needsDownload = true
      log.lifecycle 'Support library repository missing. Downloading...'

      // Add future repository to the project since the main plugin skips it when missing.
      project.repositories.maven {
        url = androidRepositoryDir
      }
    } else if (!dependenciesAvailable(supportLibraryDeps)) {
      needsDownload = true
      log.lifecycle 'Support library repository outdated. Downloading update...'
    }

    if (needsDownload) {
      def code = androidCommand.update 'extra-android-m2repository'
      if (code != 0) {
        throw new StopExecutionException("Support repository download failed with code $code.")
      }
    }
  }

  def resolvePlayServiceRepository() {
    def playServicesDeps = findDependenciesWithGroup 'com.google.android.gms'
    if (playServicesDeps.isEmpty()) {
      log.debug 'No Google Play Services dependency found.'
      return
    }

    log.debug "Found Google Play Services dependencies: $playServicesDeps"

    def needsDownload = false;
    if (!googleRepositoryDir.exists()) {
      needsDownload = true
      log.lifecycle 'Google Play Services repository missing. Downloading...'

      // Add future repository to the project since the main plugin skips it when missing.
      project.repositories.maven {
        url = googleRepositoryDir
      }
    } else if (!dependenciesAvailable(playServicesDeps)) {
      needsDownload = true
      log.lifecycle 'Google Play Services repository outdated. Downloading update...'
    }

    if (needsDownload) {
      def code = androidCommand.update 'extra-google-m2repository'
      if (code != 0) {
        throw new StopExecutionException(
            "Google Play Services repository download failed with code $code.")
      }
    }
  }

  def findDependenciesWithGroup(String group) {
    def deps = []
    for (Configuration configuration : project.configurations) {
      for (Dependency dependency : configuration.dependencies) {
        if (group.equals(dependency.group)) {
          deps.add dependency
        }
      }
    }
    return deps
  }

  def dependenciesAvailable(def deps) {
    try {
      project.configurations.detachedConfiguration(deps as Dependency[]).files
      return true
    } catch (Exception ignored) {
      return false
    }
  }
}

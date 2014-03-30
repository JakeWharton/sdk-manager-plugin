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
  final File androidExecutable

  PackageResolver(Project project, File sdk) {
    this.sdk = sdk
    this.project = project

    buildToolsDir = new File(sdk, 'build-tools')
    platformsDir = new File(sdk, 'platforms')

    def toolsDir = new File(sdk, 'tools')
    def platform = Platform.get()
    androidExecutable = new File(toolsDir, platform.androidExecutable)
    androidExecutable.setExecutable true, false
  }

  void resolve() {
    resolveBuildTools()
    resolveCompileVersion()
  }

  private void resolveBuildTools() {
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

  private void resolveCompileVersion() {
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

package com.jakewharton.sdkmanager.internal

import org.apache.commons.io.FileUtils
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.rauschig.jarchivelib.ArchiverFactory

import static com.android.SdkConstants.PLATFORM_DARWIN
import static com.android.SdkConstants.PLATFORM_LINUX
import static com.android.SdkConstants.PLATFORM_WINDOWS
import static com.android.SdkConstants.currentPlatform
import static org.rauschig.jarchivelib.ArchiveFormat.TAR
import static org.rauschig.jarchivelib.ArchiveFormat.ZIP
import static org.rauschig.jarchivelib.CompressionType.GZIP

/** Manages platform-specific SDK downloads. */
enum SdkDownload {
  WINDOWS('windows','zip'),
  LINUX('linux', 'tgz'),
  DARWIN('macosx', 'zip');

  static SdkDownload get() {
    switch (currentPlatform()) {
      case PLATFORM_WINDOWS:
        return WINDOWS
      case PLATFORM_LINUX:
        return LINUX
      case PLATFORM_DARWIN:
        return DARWIN
      default:
        throw new IllegalStateException("Unknown platform.")
    }
  }

  final static SDK_VERSION_MAJOR = 24;

  final Logger log = Logging.getLogger SdkDownload
  final String suffix
  final String ext

  SdkDownload(String suffix, String ext) {
    this.suffix = suffix
    this.ext = ext
  }

  /** Download the SDK to {@code temp} and extract to {@code dest}. */
  void download(File dest) {
    def url = "http://dl.google.com/android/android-sdk_r$SDK_VERSION_MAJOR-$suffix.$ext"
    log.debug "Downloading SDK from $url."

    File temp = new File(dest.getParentFile(), 'android-sdk.temp')
    temp.withOutputStream {
      it << new URL(url).content
    }

    // Archives have a single child folder. Extract to the parent directory.
    def parentFile = temp.getParentFile()
    log.debug "Extracting SDK to $parentFile.absolutePath."
    getArchiver().extract(temp, parentFile)

    // Move the aforementioned child folder to the real destination.
    def extracted = new File(parentFile, "android-sdk-$suffix")
    FileUtils.moveDirectory extracted, dest

    // Delete downloaded archive.
    temp.delete()
  }

  def getArchiver() {
    switch (ext) {
      case 'zip':
        return ArchiverFactory.createArchiver(ZIP)
      case 'tgz':
        return ArchiverFactory.createArchiver(TAR, GZIP)
      default:
        throw new IllegalArgumentException("Unknown archive format '$ext'.")
    }
  }
}

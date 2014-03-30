package com.jakewharton.sdkmanager.internal

import com.android.SdkConstants
import com.google.common.io.Files
import org.apache.log4j.Logger
import org.rauschig.jarchivelib.ArchiveFormat
import org.rauschig.jarchivelib.ArchiverFactory
import org.rauschig.jarchivelib.CompressionType

/** Manages platform-specific SDK downloads. */
enum SdkDownload {
  WINDOWS('windows.zip', ArchiveFormat.ZIP, null),
  LINUX('linux.tgz', ArchiveFormat.TAR, CompressionType.GZIP),
  DARWIN('macosx.zip', ArchiveFormat.ZIP, null);

  static SdkDownload get() {
    switch (SdkConstants.currentPlatform()) {
      case SdkConstants.PLATFORM_WINDOWS:
        return WINDOWS
      case SdkConstants.PLATFORM_LINUX:
        return LINUX
      case SdkConstants.PLATFORM_DARWIN:
        return DARWIN
      default:
        throw new IllegalStateException("Unknown platform '${SdkConstants.currentPlatformName()}'")
    }
  }

  final def log = Logger.getLogger SdkDownload
  final String suffix
  final ArchiveFormat format
  final CompressionType compression

  SdkDownload(String suffix, ArchiveFormat format, CompressionType compression) {
    this.suffix = suffix
    this.format = format
    this.compression = compression
  }

  /** Download the SDK to {@code temp} and extract to {@code dest}. */
  void download(File temp, File dest) {
    def url = "http://dl.google.com/android/android-sdk_r22.6.2-$suffix"
    log.debug "Downloading SDK from $url."

    temp.withOutputStream {
      it << new URL(url).content
    }

    final def archiver
    if (compression != null) {
      archiver = ArchiverFactory.createArchiver format, compression
    } else {
      archiver = ArchiverFactory.createArchiver format
    }

    def parentFile = temp.getParentFile()
    log.debug "Extracting SDK to $parentFile.absolutePath."
    archiver.extract(temp, parentFile)

    def extracted = new File(parentFile, 'android-sdk-macosx')
    Files.move extracted, dest

    temp.delete()
  }
}

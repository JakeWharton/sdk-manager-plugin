package com.jakewharton.sdkmanager.internal

import org.apache.log4j.Logger
import org.rauschig.jarchivelib.ArchiveFormat
import org.rauschig.jarchivelib.ArchiverFactory
import org.rauschig.jarchivelib.CompressionType

/** Manages platform-specific SDK downloads. */
public enum SdkDownload {
  WINDOWS('windows.zip', ArchiveFormat.ZIP),
  MAC('macosx.zip', ArchiveFormat.ZIP),
  LINUX('linux.tgz', ArchiveFormat.TAR, CompressionType.GZIP);

  final def log = Logger.getLogger SdkDownload
  final String suffix
  final ArchiveFormat format
  final CompressionType compression

  SdkDownload(String suffix, ArchiveFormat format) {
    this(suffix, format, null)
  }

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

    log.debug "Extracting SDK to $dest.absolutePath."
    archiver.extract(temp, dest)

    temp.delete()
  }

  /** Returns the appropriate {@link SdkDownload} for your system. */
  static SdkDownload get() {
    def os = System.getProperty('os.name').toLowerCase(Locale.US)
    if (os.contains('windows')) {
      return WINDOWS;
    }
    if (os.contains('mac')) {
      return MAC;
    }
    return LINUX;
  }
}

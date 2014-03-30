package com.jakewharton.sdkmanager.internal

import org.rauschig.jarchivelib.ArchiveFormat
import org.rauschig.jarchivelib.CompressionType

enum Platform {
  WINDOWS(SdkDownload.of('windows.zip', ArchiveFormat.ZIP), 'android.exe'),
  MAC(SdkDownload.of('macosx.zip', ArchiveFormat.ZIP), 'android'),
  LINUX(SdkDownload.of('linux.tgz', ArchiveFormat.TAR, CompressionType.GZIP), 'android');

  final SdkDownload sdkDownload
  final String androidExecutable

  Platform(SdkDownload sdkDownload, String androidExecutable) {
    this.sdkDownload = sdkDownload
    this.androidExecutable = androidExecutable
  }

  /** Returns the appropriate {@link Platform} for your system. */
  static Platform get() {
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

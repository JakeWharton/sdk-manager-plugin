package com.jakewharton.sdkmanager.internal

interface Downloader {
  void download(File dest)

  static final class Real implements Downloader {
    @Override void download(File dest) {
      SdkDownload.get().download(dest)
    }
  }
}

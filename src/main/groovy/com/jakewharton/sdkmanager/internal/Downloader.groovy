package com.jakewharton.sdkmanager.internal

interface Downloader {
  void download(File temp, File dest)

  static final class Real implements Downloader {
    @Override void download(File temp, File dest) {
      SdkDownload.get().download(temp, dest)
    }
  }
}

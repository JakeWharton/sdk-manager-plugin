package com.jakewharton.sdkmanager.util

import com.jakewharton.sdkmanager.internal.Downloader
import org.apache.commons.io.FileUtils

final class RecordingDownloader extends ArrayList<String> implements Downloader {
  @Override void download(File dest) {
    add 'download' as String
    FileUtils.touch dest
  }
}

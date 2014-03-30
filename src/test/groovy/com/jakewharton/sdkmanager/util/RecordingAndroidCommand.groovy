package com.jakewharton.sdkmanager.util

import com.jakewharton.sdkmanager.internal.AndroidCommand

final class RecordingAndroidCommand extends ArrayList<String> implements AndroidCommand {
  int nextReturnCode = 0

  @Override int update(String filter) {
    add("update $filter" as String)
    return nextReturnCode
  }
}

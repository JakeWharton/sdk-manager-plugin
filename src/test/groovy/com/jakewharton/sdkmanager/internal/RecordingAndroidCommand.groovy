package com.jakewharton.sdkmanager.internal

final class RecordingAndroidCommand extends ArrayList<String> implements AndroidCommand {
  int nextReturnCode = 0

  @Override int update(String filter) {
    add("update $filter" as String)
    return nextReturnCode
  }
}

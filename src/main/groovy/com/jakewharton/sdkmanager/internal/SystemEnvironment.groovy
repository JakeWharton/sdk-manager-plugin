package com.jakewharton.sdkmanager.internal;

/** An indirection to {@link System#getenv(String)}. */
class SystemEnvironment {
  // Exposed for testing.
  static SystemEnvironment instance = new SystemEnvironment();

  static String getValue(String name) {
    return instance.value(name)
  }

  String value(String name) {
    return System.getenv(name)
  }
}

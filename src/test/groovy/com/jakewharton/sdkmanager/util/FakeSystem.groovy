package com.jakewharton.sdkmanager.util

import com.jakewharton.sdkmanager.internal.System

final class FakeSystem implements System {
  final def env = new LinkedHashMap<String, String>()
  final def properties = new LinkedHashMap<String, String>()

  @Override String env(String name) {
    return env.get(name)
  }

  @Override String property(String key) {
    return properties.get(key)
  }

  @Override String property(String key, String defaultValue) {
    return properties.containsValue(key) ? properties.get(key) : defaultValue
  }
}

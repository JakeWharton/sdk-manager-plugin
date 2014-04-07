package org.gradle.testkit.functional.internal;

import java.io.File;
import java.util.List;

public interface GradleHandleFactory {
  GradleHandle start(File dir, List<String> arguments);
}

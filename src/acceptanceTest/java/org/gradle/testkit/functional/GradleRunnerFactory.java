package org.gradle.testkit.functional;

import java.io.File;
import org.gradle.testkit.functional.internal.DefaultGradleRunner;
import org.gradle.testkit.functional.internal.GradleHandleFactory;
import org.gradle.testkit.functional.internal.classpath.ClasspathInjectingGradleHandleFactory;
import org.gradle.testkit.functional.internal.toolingapi.ToolingApiGradleHandleFactory;

public final class GradleRunnerFactory {
  public static GradleRunner create(File userHome) {
    GradleHandleFactory toolingApiHandleFactory = new ToolingApiGradleHandleFactory();

    // TODO: Which class would be attached to the right classloader? Is using something from the test kit right?
    Class<?> sourceClass = GradleRunnerFactory.class;
    ClassLoader sourceClassLoader = sourceClass.getClassLoader();
    GradleHandleFactory classpathInjectingHandleFactory =
        new ClasspathInjectingGradleHandleFactory(sourceClassLoader, toolingApiHandleFactory,
            userHome);

    return new DefaultGradleRunner(classpathInjectingHandleFactory);
  }

  private GradleRunnerFactory() {
  }
}

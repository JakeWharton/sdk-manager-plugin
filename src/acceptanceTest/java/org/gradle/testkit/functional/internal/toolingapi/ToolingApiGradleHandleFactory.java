package org.gradle.testkit.functional.internal.toolingapi;

import java.io.File;
import java.util.List;
import org.gradle.testkit.functional.internal.GradleHandle;
import org.gradle.testkit.functional.internal.GradleHandleFactory;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

public class ToolingApiGradleHandleFactory implements GradleHandleFactory {
  public GradleHandle start(File directory, List<String> arguments) {
    GradleConnector connector = GradleConnector.newConnector();
    connector.forProjectDirectory(directory);
    ProjectConnection connection = connector.connect();
    BuildLauncher launcher = connection.newBuild();
    String[] argumentArray = new String[arguments.size()];
    arguments.toArray(argumentArray);
    launcher.withArguments(argumentArray);
    return new BuildLauncherBackedGradleHandle(launcher);
  }
}

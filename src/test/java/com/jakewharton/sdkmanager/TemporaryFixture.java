package com.jakewharton.sdkmanager;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class TemporaryFixture extends TemporaryFolder {
  private static final Logger LOG = Logging.getLogger(TemporaryFixture.class);
  private static final String FOLDER_PROJECT = "project";
  private static final String FOLDER_SDK = ".android-sdk";

  private final String folder;

  private String fixtureName;
  private File project;
  private File sdk;

  public TemporaryFixture(String folder) {
    this.folder = folder;
  }

  public File getProject() {
    return project;
  }

  public File getSdk() {
    return sdk;
  }

  @Override protected void before() throws Throwable {
    super.before();

    File fixtures = new File(folder);
    File from = new File(fixtures, fixtureName);
    if (!new File(from, FOLDER_PROJECT).exists()) {
      LOG.warn(String.format("Project folder not found for '%s'.", fixtureName));
    }
    if (!new File(from, FOLDER_SDK).exists()) {
      LOG.warn(String.format("SDK folder not found for '%s'.", fixtureName));
    }

    File root = getRoot();
    FileUtils.copyDirectory(from, root);

    LOG.debug(String.format("Temporary fixture folder: '%s'", root.getAbsolutePath()));

    project = new File(root, FOLDER_PROJECT);
    sdk = new File(root, FOLDER_SDK);
  }

  @Override public Statement apply(Statement base, Description description) {
    FixtureName annotation = description.getAnnotation(FixtureName.class);
    if (annotation == null) {
      throw new IllegalStateException(String.format("Test '%s' missing @FixtureName annotation.",
          description.getDisplayName()));
    }
    fixtureName = annotation.value();

    return super.apply(base, description);
  }
}

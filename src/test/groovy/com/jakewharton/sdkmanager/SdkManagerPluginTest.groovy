package com.jakewharton.sdkmanager

import com.jakewharton.sdkmanager.internal.SystemEnvironment
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

import static com.android.SdkConstants.ANDROID_HOME_ENV
import static com.android.SdkConstants.FN_LOCAL_PROPERTIES
import static com.android.SdkConstants.SDK_DIR_PROPERTY
import static org.fest.assertions.api.Assertions.assertThat

class SdkManagerPluginTest {
  @Rule public TemporaryFixture fixture = new TemporaryFixture();

  def env = new LinkedHashMap<>()
  def project

  @Before public void setUp() {
    // Redefine the user home directory to a temporary location for each test.
    System.setProperty 'user.home', fixture.root.absolutePath

    // Redirect environment variable loading to a local map.
    SystemEnvironment.instance = new SystemEnvironment() {
      @Override String value(String name) {
        return env.get(name)
      }
    }

    project = ProjectBuilder.builder()
        .withProjectDir(fixture.project)
        .build()
  }

  /** Assert that the project's local.properties {@code sdk.dir} points at {@code sdkFolder}. */
  def assertLocalProperties(File sdkFolder) {
    def localProperties = new File(fixture.project, FN_LOCAL_PROPERTIES)
    def properties = new Properties()
    localProperties.withInputStream { properties.load it }
    def sdkDirPath = properties.getProperty SDK_DIR_PROPERTY
    assertThat(sdkDirPath).isEqualTo(sdkFolder.absolutePath)
  }

  @Ignore
  @FixtureName("no-sdk")
  @Test public void noSdk() {
    assertThat(fixture.sdk).doesNotExist()
    project.evaluate()
    assertThat(fixture.sdk).exists()
    assertLocalProperties(fixture.sdk)
  }

  @Ignore
  @FixtureName("missing-local-properties")
  @Test public void missingLocalProperties() {
    assertThat(fixture.sdk).exists()
    project.evaluate()
    assertLocalProperties(fixture.sdk)
  }

  @Ignore
  @FixtureName("missing-local-properties-with-android-home")
  @Test public void missingLocalPropertiesWithAndroidHome() {
    // Point the ANDROID_HOME environment variable to the custom SDK location.
    File sdk = new File(fixture.root, 'sdk')
    env.put ANDROID_HOME_ENV, sdk.absolutePath
    assertThat(sdk.exists())

    // Make sure no normal SDK exists.
    assertThat(fixture.sdk).doesNotExist()

    project.evaluate()

    // Ensure the normal SDK still does not exist.
    assertThat(fixture.sdk).doesNotExist()

    // Ensure that the project's local.properties points at the custom SDK location.
    assertLocalProperties(sdk)
  }
}

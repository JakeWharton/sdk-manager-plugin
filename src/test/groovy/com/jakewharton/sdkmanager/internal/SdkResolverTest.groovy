package com.jakewharton.sdkmanager.internal

import com.jakewharton.sdkmanager.FixtureName
import com.jakewharton.sdkmanager.TemporaryFixture
import com.jakewharton.sdkmanager.util.FakeSystem
import com.jakewharton.sdkmanager.util.RecordingDownloader
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import static com.android.SdkConstants.FN_LOCAL_PROPERTIES
import static com.android.SdkConstants.SDK_DIR_PROPERTY
import static org.fest.assertions.api.Assertions.assertThat

class SdkResolverTest {
  @Rule public TemporaryFixture fixture = new TemporaryFixture();

  Project project
  File localProperties
  FakeSystem system
  RecordingDownloader downloader
  SdkResolver sdkResolver

  @Before public void setUp() {
    project = ProjectBuilder.builder()
        .withProjectDir(fixture.project)
        .build()
    localProperties = new File(fixture.project, FN_LOCAL_PROPERTIES)

    system = new FakeSystem()
    system.properties.put 'user.home', fixture.root.absolutePath

    downloader = new RecordingDownloader()
    sdkResolver = new SdkResolver(project, system, downloader)
  }

  /** Assert that the project's local.properties {@code sdk.dir} points at {@code sdkFolder}. */
  def assertLocalProperties(File sdkFolder) {
    assertThat(localProperties).exists()
    def properties = new Properties()
    localProperties.withInputStream { properties.load it }
    def sdkDirPath = properties.getProperty SDK_DIR_PROPERTY
    assertThat(sdkDirPath).isEqualTo(sdkFolder.absolutePath)
  }

  @FixtureName("no-sdk")
  @Test public void missingSdk() {
    assertThat(fixture.sdk).doesNotExist()
    assertThat(localProperties).doesNotExist()
    sdkResolver.resolve()
    assertThat(downloader).containsExactly('download')
    assertThat(fixture.sdk).exists()
    assertLocalProperties(fixture.sdk)
  }

  @FixtureName("missing-local-properties")
  @Test public void missingLocalProperties() {
    assertThat(fixture.sdk).exists()
    assertThat(localProperties).doesNotExist()
    sdkResolver.resolve()
    assertThat(downloader).isEmpty()
    assertLocalProperties(fixture.sdk)
  }

  @FixtureName("missing-local-properties-with-android-home")
  @Test public void missingLocalPropertiesWithAndroidHome() {
    // Point the ANDROID_HOME environment variable to the custom SDK location.
    File sdk = new File(fixture.root, 'sdk')
    system.env.put 'ANDROID_HOME', sdk.absolutePath
    assertThat(sdk).exists()
    sdkResolver.resolve()
    assertThat(downloader).isEmpty()
    assertLocalProperties(sdk)
  }
}

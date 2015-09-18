package com.jakewharton.sdkmanager.internal

import com.jakewharton.sdkmanager.FixtureName
import com.jakewharton.sdkmanager.TemporaryFixture
import com.jakewharton.sdkmanager.util.FakeSystem
import com.jakewharton.sdkmanager.util.RecordingDownloader
import org.gradle.api.Project
import org.gradle.api.tasks.StopExecutionException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import static com.android.SdkConstants.ANDROID_HOME_ENV
import static com.android.SdkConstants.FN_LOCAL_PROPERTIES
import static com.android.SdkConstants.SDK_DIR_PROPERTY
import static org.fest.assertions.api.Assertions.assertThat
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown

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
    sdkResolver = new SdkResolver(project, system, downloader, false)
  }

  def writeLocalProperties(String path) {
    localProperties.withOutputStream {
      it << "$SDK_DIR_PROPERTY=$path"
    }
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
  @Test public void windowsPathIsEscaped() {
    sdkResolver = new SdkResolver(project, system, downloader, true)
    sdkResolver.writeLocalProperties "C:\\Foo\\Bar"

    def properties = new Properties()
    localProperties.withInputStream { properties.load it }
    def sdkDirPath = properties.getProperty SDK_DIR_PROPERTY
    assertThat(sdkDirPath).isEqualTo("C:\\Foo\\Bar")
  }

  @FixtureName("no-sdk")
  @Test public void missingSdk() {
    assertThat(fixture.sdk).doesNotExist()
    assertThat(localProperties).doesNotExist()
    def resolvedSdk = sdkResolver.resolve()
    assertThat(downloader).containsExactly('download')
    assertThat(resolvedSdk).isEqualTo(fixture.sdk)
    assertThat(resolvedSdk).exists()
    assertLocalProperties(fixture.sdk)
  }

  @FixtureName("no-sdk")
  @Test public void emptyAndroidHomeEnv() {
    system.env.put ANDROID_HOME_ENV, ""
    assertThat(fixture.sdk).doesNotExist()
    assertThat(localProperties).doesNotExist()
    def resolvedSdk = sdkResolver.resolve()
    assertThat(downloader).containsExactly('download')
    assertThat(resolvedSdk).isEqualTo(fixture.sdk)
    assertThat(resolvedSdk).exists()
    assertLocalProperties(fixture.sdk)
  }

  @FixtureName("missing-local-properties")
  @Test public void missingLocalProperties() {
    assertThat(fixture.sdk).exists()
    assertThat(localProperties).doesNotExist()
    def resolvedSdk = sdkResolver.resolve()
    assertThat(downloader).isEmpty()
    assertThat(resolvedSdk).isEqualTo(fixture.sdk)
    assertLocalProperties(fixture.sdk)
  }

  @FixtureName("missing-local-properties-with-android-home")
  @Test public void missingLocalPropertiesWithAndroidHome() {
    // Point the ANDROID_HOME environment variable to the custom SDK location.
    File sdk = new File(fixture.root, 'sdk')
    system.env.put ANDROID_HOME_ENV, sdk.absolutePath
    assertThat(sdk).exists()
    def resolvedSdk = sdkResolver.resolve()
    assertThat(downloader).isEmpty()
    assertThat(resolvedSdk).isEqualTo(sdk)
    assertLocalProperties(sdk)
  }

  @FixtureName("local-properties")
  @Test public void localPropertiesExists() {
    File sdk = new File(fixture.root, 'sdk')
    writeLocalProperties(sdk.absolutePath)
    def resolvedSdk = sdkResolver.resolve()
    assertThat(downloader).isEmpty()
    assertThat(resolvedSdk).isEqualTo(sdk)
  }

  @FixtureName("local-properties-no-sdk-dir")
  @Test public void localPropertiesNoSdkDir() {
    localProperties.withOutputStream {
      it << "foo=bar\n"
    }
    def resolvedSdk = sdkResolver.resolve()
    assertThat(downloader).isEmpty()
    assertThat(resolvedSdk).isEqualTo(fixture.sdk)
    assertLocalProperties(fixture.sdk)
  }

  @FixtureName("local-properties-from-child-project")
  @Test public void localPropertiesExistsFromChildProject() {
    File sdk = new File(fixture.root, 'sdk')
    writeLocalProperties(sdk.absolutePath)

    def childProject = new ProjectBuilder()
        .withParent(project)
        .withProjectDir(new File(fixture.project, 'child'))
        .build()
    def childSdkResolver = new SdkResolver(childProject, system, downloader, false)
    def resolvedSdk = childSdkResolver.resolve()

    assertThat(downloader).isEmpty()
    assertThat(resolvedSdk).isEqualTo(sdk)
  }

  @FixtureName("invalid-local-properties")
  @Test public void invalidLocalPropertiesThrows() {
    writeLocalProperties('/invalid/pointer')
    try {
      sdkResolver.resolve()
      failBecauseExceptionWasNotThrown(StopExecutionException)
    } catch (StopExecutionException e) {
      assertThat(e).
          hasMessage(
              "Specified SDK directory '/invalid/pointer' in 'local.properties' is not found.")
    }
    assertThat(downloader).isEmpty()
  }

  @FixtureName("missing-android-home-pointer")
  @Test public void missingAndroidHomePointerDownloads() {
    File sdk = new File(fixture.root, 'ess-dee-kay')
    system.env.put ANDROID_HOME_ENV, sdk.absolutePath
    def resolvedSdk = sdkResolver.resolve()
    assertThat(downloader).containsExactly('download')
    assertThat(resolvedSdk).isEqualTo(sdk)
    assertThat(resolvedSdk).exists()
    assertLocalProperties(sdk)
  }
}

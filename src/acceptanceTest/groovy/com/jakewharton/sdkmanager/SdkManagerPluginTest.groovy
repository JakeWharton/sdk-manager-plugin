package com.jakewharton.sdkmanager
import org.gradle.testkit.functional.GradleRunner
import org.gradle.testkit.functional.GradleRunnerFactory
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import static org.fest.assertions.api.Assertions.assertThat

class SdkManagerPluginTest {
  @Rule public TemporaryFixture fixture = new TemporaryFixture('src/acceptanceTest/fixtures')

  private GradleRunner runner

  @Before public void setUp() {
    runner = GradleRunnerFactory.create(fixture.root)
    runner.directory = fixture.project
    runner.arguments.addAll 'clean', 'check', '--stacktrace'
  }

  @After public void tearDown() {
    "echo 'HIHIHIHIHIHI'".execute()
    "ls -lhR $fixture.root.absolutePath/.android-sdk/build-tools/19.1.0/".execute()
    "echo 'HIHIHIHIHIHI'".execute()
  }

  @FixtureName("child-projects")
  @Test public void childProject() {
    def result = runner.run()
    def output = result.standardOutput
    assertThat(output).contains('BUILD SUCCESSFUL')
    assertThat(output).contains('Android SDK not found. Downloading...')
    assertThat(output).contains('Build tools 19.1.0 missing. Downloading...')
    assertThat(output).contains('Build tools 19.0.3 missing. Downloading...')
    assertThat(output).contains('Compilation API android-19 missing. Downloading...')
    assertThat(output).contains('Compilation API addon-google_apis-google-19 missing. Downloading...')
    assertThat(output).contains('Support library repository missing. Downloading...')
    assertThat(output).contains('Google Play Services repository missing. Downloading...')
  }
}

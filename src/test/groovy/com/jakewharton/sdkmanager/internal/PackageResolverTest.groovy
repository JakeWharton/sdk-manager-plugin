package com.jakewharton.sdkmanager.internal

import com.jakewharton.sdkmanager.FixtureName
import com.jakewharton.sdkmanager.SdkManagerExtension
import com.jakewharton.sdkmanager.TemporaryFixture
import com.jakewharton.sdkmanager.util.RecordingAndroidCommand
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import static com.android.SdkConstants.FN_LOCAL_PROPERTIES
import static com.android.SdkConstants.SDK_DIR_PROPERTY
import static org.fest.assertions.api.Assertions.assertThat

class PackageResolverTest {
  @Rule public TemporaryFixture fixture = new TemporaryFixture();

  Project project
  RecordingAndroidCommand androidCommand
  PackageResolver packageResolver

  @Before public void setUp() {
    project = ProjectBuilder.builder()
        .withProjectDir(fixture.project)
        .build()

    // Write a local.properties file that points to the fixture SDK directory.
    new File(fixture.project, FN_LOCAL_PROPERTIES).withOutputStream {
      it << "$SDK_DIR_PROPERTY=${fixture.sdk.absolutePath}"
    }

    androidCommand = new RecordingAndroidCommand(fixture.sdk)
    packageResolver = new PackageResolver(project, fixture.sdk, androidCommand)
  }

  @FixtureName("up-to-date-build-tools")
  @Test public void upToDateBuildToolsRecognized() {
    project.apply plugin: 'android'
    project.android {
      buildToolsVersion '19.0.3'
    }

    packageResolver.resolveBuildTools()
    assertThat(androidCommand).isEmpty()
  }

  @FixtureName("missing-build-tools")
  @Test public void missingBuildToolsAreDownloaded() {
    project.apply plugin: 'android'
    project.android {
      buildToolsVersion '19.0.3'
    }

    packageResolver.resolveBuildTools()
    assertThat(androidCommand).containsExactly('update build-tools-19.0.3')
  }

  @FixtureName("outdated-build-tools")
  @Test public void outdatedBuildToolsAreDownloaded() {
    project.apply plugin: 'android'
    project.android {
      buildToolsVersion '19.0.3'
    }

    packageResolver.resolveBuildTools()
    assertThat(androidCommand).containsExactly('update build-tools-19.0.3')
  }

  @FixtureName("up-to-date-platform-tools")
  @Test public void upToDatePlatformToolsRecognized() {
    project.apply plugin: 'android'
    packageResolver.resolvePlatformTools()
    assertThat(androidCommand).isEmpty()
  }

  @FixtureName("missing-platform-tools")
  @Test public void missingPlatformToolsAreDownloaded() {
    project.apply plugin: 'android'
    packageResolver.resolvePlatformTools()
    assertThat(androidCommand).containsExactly('update platform-tools')
  }

  @FixtureName("up-to-date-compilation-api")
  @Test public void upToDateCompilationApiRecognized() {
    project.apply plugin: 'android'
    project.android {
      compileSdkVersion 19
    }

    packageResolver.resolveCompileVersion()
    assertThat(androidCommand).isEmpty()
  }

  @FixtureName("missing-compilation-api")
  @Test public void missingCompilationApiIsDownloaded() {
    project.apply plugin: 'android'
    project.android {
      compileSdkVersion 19
    }

    packageResolver.resolveCompileVersion()
    assertThat(androidCommand).containsExactly('update android-19')
  }

  @FixtureName("empty-compilation-api")
  @Test public void emptyCompilationApiIsDownloaded() {
    project.apply plugin: 'android'
    project.android {
      compileSdkVersion 19
    }

    def empty = new File(fixture.sdk, "platforms/android-19/empty.txt")
    assertThat(empty).exists()
    empty.delete()
    assertThat(empty).doesNotExist()

    packageResolver.resolveCompileVersion()
    assertThat(androidCommand).containsExactly('update android-19')
  }

  @FixtureName("up-to-date-google-compilation-api")
  @Test public void googleCompilationApiRecognized() {
    project.apply plugin: 'android'
    project.android {
      compileSdkVersion "Google Inc.:Google APIs:19"
    }

    packageResolver.resolveCompileVersion()
    assertThat(androidCommand).isEmpty()
  }

  @FixtureName("missing-compilation-api")
  @Test public void googleCompilationApiIsDownloaded() {
    project.apply plugin: 'android'
    project.android {
      compileSdkVersion "Google Inc.:Google APIs:19"
    }

    packageResolver.resolveCompileVersion()
    assertThat(androidCommand).
        containsExactly('update android-19', 'update addon-google_apis-google-19')
  }

  @FixtureName("outdated-compilation-api")
  @Test public void outdatedCompilationApiIsDownloaded() {
    project.apply plugin: 'android'
    project.android {
      compileSdkVersion 19
    }

    packageResolver.resolveCompileVersion()
    assertThat(androidCommand).containsExactly('update android-19')
  }

  @FixtureName("missing-compilation-api")
  @Test public void googleGdkIsDownloaded() {
    project.apply plugin: 'android'
    project.android {
      compileSdkVersion "Google Inc.:Glass Development Kit Preview:19"
    }

    packageResolver.resolveCompileVersion()
    assertThat(androidCommand).containsExactly('update addon-google_gdk-google-19')
  }

  @FixtureName("outdated-compilation-api")
  @Test public void outdatedGoogleGdkIsDownloaded() {
    project.apply plugin: 'android'
    project.android {
      compileSdkVersion "Google Inc.:Glass Development Kit Preview:19"
    }

    packageResolver.resolveCompileVersion()
    assertThat(androidCommand).containsExactly('update addon-google_gdk-google-19')
  }

  @FixtureName("missing-android-m2repository")
  @Test public void noSupportLibraryOrTestingDependencyDoesNotDownload() {
    project.apply plugin: 'android'

    packageResolver.resolveSupportLibraryRepository()
    assertThat(androidCommand).isEmpty()
  }

  @FixtureName("up-to-date-android-m2repository")
  @Test public void upToDateSupportLibraryRepositoryRecognized() {
    project.apply plugin: 'android'
    project.dependencies {
      compile 'com.android.support:support-v4:19.1.0'
    }

    packageResolver.resolveSupportLibraryRepository()
    assertThat(androidCommand).isEmpty()
  }

  @FixtureName("missing-android-m2repository")
  @Test public void missingSupportLibraryRepositoryIsDownloaded() {
    project.apply plugin: 'android'
    project.dependencies {
      compile 'com.android.support:support-v4:19.1.0'
    }

    packageResolver.resolveSupportLibraryRepository()
    assertThat(androidCommand).containsExactly('update extra-android-m2repository')
  }

  @FixtureName("missing-android-m2repository")
  @Test public void missingSupportTestingRepositoryIsDownloaded() {
    project.apply plugin: 'android'
    project.dependencies {
      compile 'com.android.support.test.espresso:espresso-core:2.0'
    }

    packageResolver.resolveSupportLibraryRepository()
    assertThat(androidCommand).containsExactly('update extra-android-m2repository')
  }

  @FixtureName("outdated-android-m2repository")
  @Test public void outdatedSupportLibraryRepositoryIsDownloaded() {
    project.apply plugin: 'android'
    project.dependencies {
      compile 'com.android.support:support-v4:19.1.0'
    }

    packageResolver.resolveSupportLibraryRepository()
    assertThat(androidCommand).containsExactly('update extra-android-m2repository')
  }

  @FixtureName("outdated-android-m2repository")
  @Test public void outdatedSupportTestingRepositoryIsDownloaded() {
    project.apply plugin: 'android'
    project.dependencies {
      compile 'com.android.support.test.espresso:espresso-core:2.0'
    }

    packageResolver.resolveSupportLibraryRepository()
    assertThat(androidCommand).containsExactly('update extra-android-m2repository')
  }

  @FixtureName("missing-google-m2repository")
  @Test public void noPlayServicesDependencyDoesNotDownload() {
    project.apply plugin: 'android'

    packageResolver.resolvePlayServiceRepository()
    assertThat(androidCommand).isEmpty()
  }

  @FixtureName("up-to-date-google-m2repository")
  @Test public void upToDatePlayServicesRepositoryRecognized() {
    project.apply plugin: 'android'
    project.dependencies {
      compile 'com.google.android.gms:play-services:4.3.23'
    }

    packageResolver.resolvePlayServiceRepository()
    assertThat(androidCommand).isEmpty()
  }

  @FixtureName("missing-google-m2repository")
  @Test public void missingPlayServicesRepositoryDownloaded() {
    project.apply plugin: 'android'
    project.dependencies {
      compile 'com.google.android.gms:play-services:4.3.23'
    }

    packageResolver.resolvePlayServiceRepository()
    assertThat(androidCommand).containsExactly('update extra-google-m2repository')
  }

  @FixtureName("outdated-google-m2repository")
  @Test public void outdatedPlayServicesRepositoryDownloaded() {
    project.apply plugin: 'android'
    project.dependencies {
      compile 'com.google.android.gms:play-services:4.3.23'
    }

    packageResolver.resolvePlayServiceRepository()
    assertThat(androidCommand).containsExactly('update extra-google-m2repository')
  }

  @FixtureName("no-emulator-version-specified")
  @Test public void noEmulatorVersionSpecified() {
    project.apply plugin: 'android'
    project.extensions.create("sdkManager", SdkManagerExtension)

    packageResolver.resolveEmulator()
    assertThat(androidCommand).isEmpty()
  }

  @FixtureName("up-to-date-emulator")
  @Test public void upToDateEmulatorRecognized() {
    project.apply plugin: 'android'
    project.extensions.create("sdkManager", SdkManagerExtension)
    project.sdkManager {
      emulatorVersion 'android-19'
      emulatorArchitecture 'armeabi-v7a'
    }

    packageResolver.resolveEmulator()
    assertThat(androidCommand).doesNotContain('update sys-img-armeabi-v7a-android-19')
  }

  @FixtureName("missing-emulator")
  @Test public void missingEmulatorDownloaded() {
    project.apply plugin: 'android'
    project.extensions.create("sdkManager", SdkManagerExtension)
    project.sdkManager {
      emulatorVersion 'android-19'
      emulatorArchitecture 'armeabi-v7a'
    }

    packageResolver.resolveEmulator()
    assertThat(androidCommand).contains('update sys-img-armeabi-v7a-android-19')
  }

  @FixtureName("missing-emulator")
  @Test public void emulatorDefaultsToARM() {
    project.apply plugin: 'android'
    project.extensions.create("sdkManager", SdkManagerExtension)
    project.sdkManager {
      emulatorVersion 'android-19'
    }

    packageResolver.resolveEmulator()
    assertThat(androidCommand).contains('update sys-img-armeabi-v7a-android-19')
  }

  @FixtureName("outdated-emulator")
  @Test public void outdatedEmulatorDownloaded() {
    project.apply plugin: 'android'
    project.extensions.create("sdkManager", SdkManagerExtension)
    project.sdkManager {
      emulatorVersion 'android-19'
      emulatorArchitecture 'armeabi-v7a'
    }

    packageResolver.resolveEmulator()
    assertThat(androidCommand).contains('update sys-img-armeabi-v7a-android-19')
  }
}

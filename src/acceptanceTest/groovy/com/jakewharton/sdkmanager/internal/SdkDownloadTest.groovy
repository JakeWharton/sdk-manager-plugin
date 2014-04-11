package com.jakewharton.sdkmanager.internal

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

import static com.android.SdkConstants.FD_TOOLS
import static org.fest.assertions.api.Assertions.assertThat

@RunWith(Parameterized)
class SdkDownloadTest {
  @Parameterized.Parameters(name = "{0}")
  public static Iterable<Object[]> data() {
    return SdkDownload.values().collect { [it] as Object[] }
  }

  @Parameterized.Parameter
  public SdkDownload sdkDownload;

  @Rule public TemporaryFolder temp = new TemporaryFolder()

  @Test public void downloadAndExtract() {
    def download = new File(temp.root, 'android-sdk.temp')
    def destination = new File(temp.root, 'destination')

    assertThat(download).doesNotExist()
    assertThat(destination).doesNotExist()

    sdkDownload.download destination

    assertThat(download).doesNotExist()
    assertThat(destination).exists()

    def tools = new File(destination, FD_TOOLS)
    assertThat(tools).exists()

    switch (sdkDownload) {
      case SdkDownload.DARWIN:
      case SdkDownload.LINUX:
        def android = new File(tools, 'android')
        assertThat(android).exists();
        assertThat(android.canExecute()).isTrue()
        break;
      case SdkDownload.WINDOWS:
        def android = new File(tools, 'android.bat')
        assertThat(android).exists()
        break;
      default:
        throw new IllegalStateException("Unknown platform: " + sdkDownload);
    }
  }
}

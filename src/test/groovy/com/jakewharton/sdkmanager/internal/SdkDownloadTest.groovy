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
    return SdkDownload.values().collect { [ it ] as Object[] }
  }

  @Parameterized.Parameter
  public SdkDownload sdkDownload;

  @Rule public TemporaryFolder temp = new TemporaryFolder()

  @Test public void downloadAndExtract() {
    def download = new File(temp.root, 'download')
    def destination = new File(temp.root, 'destination')

    assertThat(download).doesNotExist()
    assertThat(destination).doesNotExist()

    sdkDownload.download download, destination

    assertThat(download).doesNotExist()
    assertThat(destination).exists()
    assertThat(new File(destination, FD_TOOLS)).exists()
  }
}

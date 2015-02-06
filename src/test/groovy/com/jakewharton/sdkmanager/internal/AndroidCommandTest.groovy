package com.jakewharton.sdkmanager.internal

import com.jakewharton.sdkmanager.util.FakeSystem
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static com.android.SdkConstants.FD_TOOLS
import static com.android.SdkConstants.androidCmdName
import static org.fest.assertions.api.Assertions.assertThat

class AndroidCommandTest {
  @Rule public TemporaryFolder sdk = new TemporaryFolder()

  FakeSystem system = new FakeSystem()
  AndroidCommand.Real command
  String exe

  @Before void setUp() {
    command = new AndroidCommand.Real(sdk.root, system)
    exe = new File(new File(sdk.root, FD_TOOLS), androidCmdName()).absolutePath
  }

  @Test public void simple() {
    def command = command.generateCommand('update', ['-a'])
    assertThat(command).containsExactly(exe, 'update', 'sdk', '-u', '-a')
  }

  @Test public void proxy() {
    system.properties.put 'http.proxyHost', 'example.com'
    system.properties.put 'http.proxyPort', '1234'
    def command = command.generateCommand('update', ['-a'])
    assertThat(command).
        containsExactly(exe, 'update', 'sdk', '-u', '-a', '--proxy-host', 'example.com',
            '--proxy-port', '1234')
  }

  @Test public void proxyHostRequiresPort() {
    system.properties.put 'http.proxyHost', 'example.com'
    def command = command.generateCommand('update', ['-a'])
    assertThat(command).containsExactly(exe, 'update', 'sdk', '-u', '-a')
  }

  @Test public void proxyPortRequiresHost() {
    system.properties.put 'http.proxyPort', '1234'
    def command = command.generateCommand('update', ['-a'])
    assertThat(command).containsExactly(exe, 'update', 'sdk', '-u', '-a')
  }
}

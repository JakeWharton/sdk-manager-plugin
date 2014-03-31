package com.jakewharton.sdkmanager.internal

import org.apache.log4j.Logger

import static com.android.SdkConstants.FD_TOOLS
import static com.android.SdkConstants.androidCmdName

interface AndroidCommand {
  int update(String filter);

  static final class Real implements AndroidCommand {
    final def log = Logger.getLogger Real
    final File androidExecutable

    Real(File sdk) {
      def toolsDir = new File(sdk, FD_TOOLS)
      androidExecutable = new File(toolsDir, androidCmdName())
      androidExecutable.setExecutable true, false
    }

    @Override int update(String filter) {
      // -a == all
      // -u == no UI
      // -t == filter
      def cmd = [androidExecutable.absolutePath, 'update', 'sdk', '-a', '-u', '-t', filter]
      def process = cmd.execute()

      // Press 'y' and then enter on the license prompt.
      def output = new OutputStreamWriter(process.out)
      output.write("y\n")
      output.close()

      // Pipe the command output to our log.
      def input = new InputStreamReader(process.in)
      def line
      while ((line = input.readLine()) != null) {
        log.debug line
      }

      return process.waitFor()
    }
  }
}

package org.gradle.testkit.functional.internal.classpath;

import java.io.File;
import java.io.Writer;
import java.util.List;
import org.gradle.internal.ErroringAction;
import org.gradle.internal.IoActions;
import org.gradle.util.TextUtil;

public class ClasspathAddingInitScriptBuilder {
  public void build(File initScriptFile, final List<File> classpath, final File userHome) {
    IoActions.writeTextFile(initScriptFile, new ErroringAction<Writer>() {
      @Override
      protected void doExecute(Writer writer) throws Exception {
        writer.write("System.setProperty('user.home', '" + userHome.getAbsolutePath() + "')\n");

        // Since we cannot control the environment, set flags for the plugin to ignore them.
        writer.write("System.setProperty('com.jakewharton.sdkmanager.ignore_android_home', 'true')\n");

        writer.write("allprojects {\n");
        writer.write("  buildscript {\n");
        writer.write("    dependencies {\n");
        writer.write("      classpath files(\n");
        int i = 0;
        for (File file : classpath) {
          writer.write(
              String.format("        '%s'", TextUtil.escapeString(file.getAbsolutePath())));
          if (++i != classpath.size()) {
            writer.write(",\n");
          }
        }
        writer.write("\n");
        writer.write("      )\n");
        writer.write("    }\n");
        writer.write("  }\n");
        writer.write("}\n");
      }
    });
  }
}

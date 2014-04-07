package org.gradle.testkit.functional.internal.classpath;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.gradle.api.Transformer;
import org.gradle.internal.UncheckedException;
import org.gradle.internal.classloader.ClasspathUtil;
import org.gradle.testkit.functional.internal.GradleHandle;
import org.gradle.testkit.functional.internal.GradleHandleFactory;
import org.gradle.util.CollectionUtils;
import org.gradle.util.GFileUtils;

public class ClasspathInjectingGradleHandleFactory implements GradleHandleFactory {
  private final ClassLoader classLoader;
  private final GradleHandleFactory delegateFactory;
  private final File userHome;

  public ClasspathInjectingGradleHandleFactory(ClassLoader classLoader,
      GradleHandleFactory delegateFactory, File userHome) {
    this.classLoader = classLoader;
    this.delegateFactory = delegateFactory;
    this.userHome = userHome;
  }

  @Override public GradleHandle start(File directory, List<String> arguments) {
    File testKitDir = new File(directory, ".gradle-test-kit");
    if (!testKitDir.exists()) {
      GFileUtils.mkdirs(testKitDir);
    }

    File initScript = new File(testKitDir, "init.gradle");
    new ClasspathAddingInitScriptBuilder().build(initScript, getClasspathAsFiles(), userHome);

    List<String> ammendedArguments = new ArrayList<String>(arguments.size() + 2);
    ammendedArguments.add("-I");
    ammendedArguments.add(initScript.getAbsolutePath());
    ammendedArguments.addAll(arguments);
    return delegateFactory.start(directory, ammendedArguments);
  }

  private List<File> getClasspathAsFiles() {
    List<URL> classpathUrls = ClasspathUtil.getClasspath(classLoader);
    return CollectionUtils.collect(classpathUrls, new ArrayList<File>(classpathUrls.size()),
        new Transformer<File, URL>() {
          public File transform(URL url) {
            try {
              return new File(url.toURI());
            } catch (URISyntaxException e) {
              throw UncheckedException.throwAsUncheckedException(e);
            }
          }
        }
    );
  }
}

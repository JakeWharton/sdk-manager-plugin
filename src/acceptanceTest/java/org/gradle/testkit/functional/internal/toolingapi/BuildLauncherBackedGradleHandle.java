/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.testkit.functional.internal.toolingapi;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.gradle.testkit.functional.ExecutionResult;
import org.gradle.testkit.functional.internal.DefaultExecutionResult;
import org.gradle.testkit.functional.internal.GradleHandle;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ResultHandler;

public class BuildLauncherBackedGradleHandle implements GradleHandle {

  final private ByteArrayOutputStream standardOutput = new ByteArrayOutputStream();
  final private ByteArrayOutputStream standardError = new ByteArrayOutputStream();

  private final CountDownLatch runningLatch = new CountDownLatch(1);
  private final AtomicBoolean running = new AtomicBoolean(true);

  private RuntimeException exception;

  public BuildLauncherBackedGradleHandle(BuildLauncher launcher) {
    launcher.setStandardOutput(standardOutput);
    launcher.setStandardError(standardError);

    launcher.run(new ResultHandler<Void>() {
      public void onComplete(Void result) {
        finish();
      }

      public void onFailure(GradleConnectionException failure) {
        exception = failure;
        finish();
      }
    });
  }

  private void finish() {
    running.set(false);
    runningLatch.countDown();
  }

  public String getStandardOutput() {
    return standardOutput.toString();
  }

  public String getStandardError() {
    return standardError.toString();
  }

  public ExecutionResult waitForFinish() {
    try {
      runningLatch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    if (exception == null) {
      return new DefaultExecutionResult(getStandardOutput(), getStandardError());
    } else {
      throw exception;
    }
  }

  public boolean isRunning() {
    return running.get();
  }
}

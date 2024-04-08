/**
 * Copyright © 2024 Apple Inc. and the Pkl project authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pkl.gradle.task;

import java.io.File;
import javax.inject.Inject;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectories;
import org.gradle.api.tasks.OutputFiles;
import org.pkl.cli.CliEvaluator;
import org.pkl.cli.CliEvaluatorOptions;

public abstract class EvalTask extends ModulesTask {

  // not tracked because it might contain placeholders
  @Internal
  public abstract RegularFileProperty getOutputFile();

  // not tracked because it might contain placeholders
  @Internal
  public abstract DirectoryProperty getMultipleFileOutputDir();

  @Input
  @Optional
  public abstract Property<String> getOutputFormat();

  @Input
  @Optional
  public abstract Property<String> getModuleOutputSeparator();

  @Input
  @Optional
  public abstract Property<String> getExpression();

  @Inject
  public abstract ObjectFactory getObjectFactory();

  private CliEvaluator getCliEvaluator() {
    return new CliEvaluator(
        new CliEvaluatorOptions(
            getCliBaseOptions(),
            getOutputFile().get().getAsFile().getAbsolutePath(),
            getOutputFormat().get(),
            getModuleOutputSeparator().get(),
            mapAndGetOrNull(getMultipleFileOutputDir(), (it) -> it.getAsFile().getAbsolutePath()),
            getExpression().get()));
  }

  @OutputFiles
  @Optional
  public SetProperty<File> getOutputPaths() {
    var ret = getObjectFactory().setProperty(File.class);
    ret.value(getCliEvaluator().getOutputFiles()).disallowChanges();
    return ret;
  }

  @OutputDirectories
  @Optional
  public SetProperty<File> getMultipleFileOutputPaths() {
    var ret = getObjectFactory().setProperty(File.class);
    ret.value(getCliEvaluator().getOutputDirectories()).disallowChanges();
    return ret;
  }

  @Override
  protected void doRunTask() {
    //noinspection ResultOfMethodCallIgnored
    getOutputs().getPreviousOutputFiles().forEach(File::delete);
    getCliEvaluator().run();
  }
}

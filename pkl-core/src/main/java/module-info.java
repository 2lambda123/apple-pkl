/**
 * Copyright © 2024 Apple Inc. and the Pkl project authors. All rights reserved.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>https://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Pkl: Core.
 */
@SuppressWarnings("module") open module pkl.core {
  requires java.base;
  requires java.naming;

  requires org.graalvm.nativeimage;
  requires org.graalvm.truffle;

  requires org.snakeyaml.engine.v2;

  exports org.pkl.core.module to pkl.cli;
  exports org.pkl.core.packages to pkl.cli;
  exports org.pkl.core.project to pkl.cli;
  exports org.pkl.core.repl to pkl.cli;
  exports org.pkl.core.resource to pkl.cli;
  exports org.pkl.core.runtime to pkl.cli;
  exports org.pkl.core.stdlib.test.report to pkl.cli;
  exports org.pkl.core.util to pkl.cli;
  exports org.pkl.core.plugin;
  exports org.pkl.core;

  uses org.pkl.core.StackFrameTransformer;
  uses org.pkl.core.module.ModuleKeyFactory;
  uses org.pkl.core.plugin.PklPlugin;

  provides com.oracle.truffle.api.provider.TruffleLanguageProvider with
    org.pkl.core.runtime.VmLanguageProvider;
}

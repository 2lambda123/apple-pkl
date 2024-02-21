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
package org.pkl.core.plugin;

import org.pkl.core.util.Nullable;

/**
 * Pkl Plugin Error API
 *
 * <p>Describes the API expected to be implemented by all thrown exceptions within the scope of a
 * {@link PklPlugin}'s hook methods.</p>
 *
 * <p>Exceptions which don't follow this interface will be swallowed.</p>
 *
 * <p>The `E` generic parameter is expected to be an enumeration of error codes which relate to
 * this plug-in type.</p>
 */
public interface PklPluginError<E extends Enum<E>> {
  /**
   * @return Local error type
   */
  E getType();

  /**
   * @return Name of the error that occurred
   */
  String getName();

  /**
   * @return Optional description of the error that occurred
   */
  default @Nullable String getDescription() {
    return null;
  }

  /**
   * @return Informs the plugin runtime that an error was fatal; defaults to `true`.
   */
  default Boolean getFatal() {
    return true;
  }
}

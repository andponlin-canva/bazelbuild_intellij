/*
 * Copyright 2023 The Bazel Authors. All rights reserved.
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
package com.google.idea.blaze.base.qsync;

import com.google.idea.blaze.exception.BuildException;

/**
 * No dependencies were built as part of a "build dependencies" action, perhaps due to a build file
 * error.
 */
public class NoDependenciesBuiltException extends BuildException {

  public NoDependenciesBuiltException(String message) {
    super(message);
  }

  @Override
  public boolean isIdeError() {
    return false;
  }
}
/*
 * Copyright 2017 Rakuten, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rakuten.tech.mobile.datastore.keys;

import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.NotNull;

/**
 * Key coder that just converts the key to UTF-8.
 *
 * @since 0.1 {@inheritDoc}
 */
public final class Utf8KeyCoder implements KeyCoder {

  @Override
  public @NotNull CodedKey encode(final @NotNull String clearKey) {
    return new CodedKey(clearKey.getBytes(StandardCharsets.UTF_8));
  }
}

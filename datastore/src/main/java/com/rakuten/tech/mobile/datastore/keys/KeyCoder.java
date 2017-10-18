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

import java.security.GeneralSecurityException;
import org.jetbrains.annotations.NotNull;

/**
 * Encode a clear-text string using a stable, repeatable, one-way transform to produce a key that
 * can be used with a {@link com.rakuten.tech.mobile.datastore.tables.BlobTable}.
 *
 * @since 0.1
 */
public interface KeyCoder {

  /**
   * Create a new coded key from a clear-text representation.
   *
   * @param clearKey Clear-text representation of the key.
   * @return New {@code CodedKey} instance.
   * @throws GeneralSecurityException The key could not be encoded.
   * @since 0.1
   */
  @NotNull CodedKey encode(final @NotNull String clearKey) throws GeneralSecurityException;
}

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

import com.rakuten.tech.mobile.datastore.tables.BlobTable;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the key of a blob in a {@link BlobTable}.
 *
 * @since 0.1 {@inheritDoc}
 */
public final class CodedKey implements Comparable<CodedKey> {

  private final byte[] bytes;

  /**
   * Create a new instance.
   *
   * @param bytes Content of the key.
   * @since 0.1
   */
  public CodedKey(final @NotNull byte[] bytes) {
    this.bytes = Arrays.copyOf(bytes, bytes.length);
  }

  /**
   * Content of the key.
   *
   * @return Content of the key.
   * @since 0.1
   */
  public byte[] getBytes() {
    return Arrays.copyOf(bytes, bytes.length);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(bytes);
  }

  @Override
  public boolean equals(final Object o) {
    return o instanceof CodedKey && Arrays.equals(bytes, ((CodedKey) o).bytes);
  }

  @Override
  public int compareTo(final @NotNull CodedKey codedKey) {
    return Arrays.equals(bytes, codedKey.bytes) ? 0
        : Arrays.hashCode(bytes) < Arrays.hashCode(codedKey.bytes) ? 1 : -1;
  }
}

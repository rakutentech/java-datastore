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

package com.rakuten.tech.mobile.datastore.tables;

import com.rakuten.tech.mobile.datastore.keys.CodedKey;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * CodedKey-value store where values are byte arrays, or "blobs".
 *
 * @since 0.1
 */
public interface BlobTable {

  /**
   * An iterator for looping through this blob table.
   *
   * @return Iterator.
   * @throws IOException The content of the table cannot be iterated upon.
   * @since 0.1
   */
  @NotNull
  Iterator<CodedKey> iterator() throws IOException;

  /**
   * Tells whether a blob is available in the table for the specified codedKey.
   *
   * @param codedKey The blob's coded key.
   * @return Whether a blob exists.
   * @throws IOException Looking up the key failed.
   * @since 0.1
   */
  boolean contains(final @NotNull CodedKey codedKey) throws IOException;

  /**
   * Try to read a blob from the table.
   *
   * @param codedKey The blob's coded key.
   * @return Blob, or {@code null} if none was found that matches the codedKey.
   * @throws IOException Looking up the blob failed.
   * @since 0.1
   */
  @Nullable
  ByteBuffer get(final @NotNull CodedKey codedKey) throws IOException;

  /**
   * Try to write a blob into the table.
   *
   * @param codedKey The blob's coded key.
   * @param blob Blob. Implementing classes should accept {@code null} values and treat those calls
   *     the same way as a call to {@link #remove(CodedKey)}.
   * @throws IOException Writing the blob failed.
   * @since 0.1
   */
  void put(final @NotNull CodedKey codedKey, final @Nullable ByteBuffer blob) throws IOException;

  /**
   * Try to delete a blob from the table.
   *
   * @param codedKey The blob's coded key.
   * @throws IOException The blob could not be deleted.
   * @since 0.1
   */
  void remove(final @NotNull CodedKey codedKey) throws IOException;

  /**
   * Try to delete every blob in the table.
   *
   * @throws IOException At least one blob could not be deleted.
   * @since 0.1
   */
  void clear() throws IOException;
}

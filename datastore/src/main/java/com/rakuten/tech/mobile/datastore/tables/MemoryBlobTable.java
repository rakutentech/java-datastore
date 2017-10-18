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
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A memory-backed {@link BlobTable}.
 *
 * <p>Each instance internally uses a {@link ConcurrentHashMap} to manage its data. When an instance
 * gets released, all the data that it was storing is released as well.
 *
 * @since 0.1 {@inheritDoc}
 */
public class MemoryBlobTable implements BlobTable {

  private final Map<CodedKey, byte[]> store;

  /**
   * Creates a new table.
   *
   * @since 0.1
   */
  public MemoryBlobTable() {
    store = new ConcurrentHashMap<>();
  }

  @Override
  public boolean contains(final @NotNull CodedKey codedKey) throws IOException {
    return store.containsKey(codedKey);
  }

  @NotNull
  @Override
  public Iterator<CodedKey> iterator() throws IOException {
    return BlobTableIterator.of(this, store.keySet().iterator());
  }

  @Nullable
  @Override
  public ByteBuffer get(final @NotNull CodedKey codedKey) throws IOException {
    final byte[] value = store.get(codedKey);

    if (value == null) {
      return null;
    }

    return ByteBuffer.wrap(Arrays.copyOf(value, value.length));
  }

  @Override
  public void put(
      final @NotNull CodedKey codedKey,
      final @Nullable ByteBuffer blob) throws IOException {

    if (blob == null) {
      remove(codedKey);
    } else {
      final byte[] bytes = new byte[blob.remaining()];
      blob.slice().get(bytes);
      store.put(codedKey, bytes);
    }
  }

  @Override
  public void remove(final @NotNull CodedKey codedKey) throws IOException {
    store.remove(codedKey);
  }

  @Override
  public void clear() throws IOException {
    store.clear();
  }
}

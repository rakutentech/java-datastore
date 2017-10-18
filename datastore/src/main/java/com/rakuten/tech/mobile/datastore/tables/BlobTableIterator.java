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
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;

public final class BlobTableIterator implements Iterator<CodedKey> {

  private final BlobTable table;
  private final Iterator<CodedKey> keyIterator;
  private CodedKey lastCodedKey;

  private BlobTableIterator(final @NotNull BlobTable table, final Iterator<CodedKey> keyIterator) {
    this.table = table;
    this.keyIterator = keyIterator;
  }

  public static BlobTableIterator of(
      final @NotNull BlobTable table,
      final Iterator<CodedKey> keyIterator) {
    return new BlobTableIterator(table, keyIterator);
  }

  @Override
  public boolean hasNext() {
    return keyIterator.hasNext();
  }

  @Override
  public CodedKey next() {
    lastCodedKey = keyIterator.next();
    return lastCodedKey;
  }

  @Override
  public void remove() {
    if (lastCodedKey == null) {
      throw new IllegalStateException("No element to remove. Forgot a call to #next()?");
    }

    try {
      table.remove(lastCodedKey);
    } catch (IOException e) {
      throw new UnsupportedOperationException("Could not remove element", e);
    } finally {
      lastCodedKey = null;
    }
  }
}

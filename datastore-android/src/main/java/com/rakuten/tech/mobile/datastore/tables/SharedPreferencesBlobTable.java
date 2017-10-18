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

import android.content.Context;
import android.content.SharedPreferences;
import com.rakuten.tech.mobile.datastore.keys.CodedKey;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Blob table that uses {@link SharedPreferences} for storing its data.
 *
 * <p>For performance reasons, consider using a {@link FileBlobTable instead} unless working under
 * the assumption of a small number of blobs and a small blob size.
 *
 * @since 1.0
 */
public class SharedPreferencesBlobTable implements BlobTable {

  private final SharedPreferences prefs;

  public SharedPreferencesBlobTable(final @NotNull Context context, final @NotNull String name) {
    prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE);
  }

  @NotNull
  @Override
  public Iterator<CodedKey> iterator() {
    final Iterator<String> wrapped = prefs.getAll().keySet().iterator();
    return BlobTableIterator.of(this, new Iterator<CodedKey>() {
      @Override
      public boolean hasNext() {
        return wrapped.hasNext();
      }

      @Override
      public CodedKey next() {
        return new CodedKey(Base64.decodeBase64(wrapped.next()));
      }

      @Override
      public void remove() {
        wrapped.remove();
      }
    });
  }

  @Override
  public boolean contains(final @NotNull CodedKey codedKey) {
    return prefs.contains(getPreferenceNameForKey(codedKey));
  }

  @Nullable
  @Override
  public ByteBuffer get(final @NotNull CodedKey codedKey) {
    final String base64 = prefs.getString(getPreferenceNameForKey(codedKey), null);
    if (base64 == null) {
      return null;
    }

    return ByteBuffer.wrap(Base64.decodeBase64(base64));
  }

  @Override
  public void put(
      final @NotNull CodedKey codedKey,
      final @Nullable ByteBuffer blob) {

    if (blob == null) {
      remove(codedKey);
      return;
    }

    final byte[] bytes = new byte[blob.remaining()];
    blob.slice().get(bytes);

    prefs.edit()
        .putString(getPreferenceNameForKey(codedKey), Base64.encodeBase64String(bytes))
        .apply();
  }

  @Override
  public void remove(final @NotNull CodedKey codedKey) {
    prefs.edit()
        .remove(getPreferenceNameForKey(codedKey))
        .apply();
  }

  @Override
  public void clear() throws IOException {
    prefs.edit().clear().apply();
  }

  private String getPreferenceNameForKey(final @NotNull CodedKey codedKey) {
    return Base64.encodeBase64URLSafeString(codedKey.getBytes());
  }
}
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

package com.rakuten.tech.mobile.datastore;

import com.rakuten.tech.mobile.datastore.crypto.CryptoOperations;
import com.rakuten.tech.mobile.datastore.keys.CodedKey;
import com.rakuten.tech.mobile.datastore.keys.KeyCoder;
import com.rakuten.tech.mobile.datastore.tables.BlobTable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A coordinator class for this library, the {@code DataStore} orchestrates the reading and writing
 * of blobs to and from a {@link BlobTable}, encryption of the blobs using {@link CryptoOperations},
 * and the coding or obfuscation of the keys using a {@link KeyCoder}.
 *
 * <p>This is the main entry point of this library.
 *
 * <h3>Usage</h3>
 *
 * <p>The simplest usage is shown below. It uses an in-memory blob table, doesn't encode keys and
 * doesn't use encryption.
 *
 * <pre><code>
 *   final DataStore store = new DataStore(
 *       new MemoryBlobTable(),
 *       new NullCryptoOperations(),
 *       new Utf8KeyCoder());
 *
 *   store.put("foo", "hello".getBytes("UTF-8"));
 *   String hello = new String(store.get("foo"), "UTF-8");
 * </code></pre>
 *
 * @since 0.1
 */
public class DataStore {

  private final @NotNull BlobTable blobTable;
  private final @NotNull CryptoOperations cryptoOperations;
  private final @NotNull KeyCoder keyCoder;

  /**
   * Construct a new datastore instance.
   *
   * @param blobTable Concrete {@link BlobTable} instance.
   * @param cryptoOperations Concrete {@link CryptoOperations} instance.
   * @param keyCoder Concrete {@link KeyCoder} instance.
   * @since 0.1
   */
  public DataStore(
      final @NotNull BlobTable blobTable,
      final @NotNull CryptoOperations cryptoOperations,
      final @NotNull KeyCoder keyCoder) {

    this.blobTable = blobTable;
    this.cryptoOperations = cryptoOperations;
    this.keyCoder = keyCoder;
  }

  /**
   * Get the blob table managed by this instance.
   *
   * @return A {@link BlobTable} object.
   * @since 0.1
   */
  @NotNull
  public BlobTable getBlobTable() {
    return blobTable;
  }

  /**
   * Get the crypto operations used by this instance.
   *
   * @return A {@link CryptoOperations} object.
   * @since 0.1
   */
  @NotNull
  public CryptoOperations getCryptoOperations() {
    return cryptoOperations;
  }

  /**
   * Get the key coder used by this instance.
   *
   * @return A {@link KeyCoder} object.
   * @since 0.1
   */
  @NotNull
  public KeyCoder getKeyCoder() {
    return keyCoder;
  }

  /**
   * Read a blob from the data store.
   *
   * @param key The requested blob's key.
   * @return The blob's content if found, {@code null} otherwise.
   * @throws FailedDatastoreOperationException Reading from the data store failed.
   * @since 0.1
   */
  @Nullable
  public ByteBuffer get(final @NotNull String key) throws FailedDatastoreOperationException {
    try {
      final CodedKey codedKey = keyCoder.encode(key);
      final ByteBuffer encrypted = blobTable.get(codedKey);
      if (encrypted == null) {
        return null;
      } else {
        return cryptoOperations.decrypt(encrypted);
      }
    } catch (GeneralSecurityException e) {
      throw new FailedDatastoreOperationException("Crypto operation failed", e);
    } catch (IOException e) {
      throw new FailedDatastoreOperationException("Failed to read from data store", e);
    }
  }

  /**
   * Save a blob into the data store.
   *
   * @param key The blob's key.
   * @param blob The blob's content. Passing {@code null} is equivalent to calling {@link
   * #remove(String)}.
   * @throws FailedDatastoreOperationException Writing to the data store failed.
   * @since 0.1
   */
  public void put(final @NotNull String key, final @Nullable ByteBuffer blob)
      throws FailedDatastoreOperationException {
    try {
      final CodedKey codedKey = keyCoder.encode(key);
      if (blob == null) {
        blobTable.remove(codedKey);
      } else {
        blobTable.put(codedKey, cryptoOperations.encrypt(blob.slice()));
      }
    } catch (GeneralSecurityException e) {
      throw new FailedDatastoreOperationException("Crypto operation failed", e);
    } catch (IOException e) {
      throw new FailedDatastoreOperationException("Failed to write in data store", e);
    }
  }

  /**
   * Remove a blob from the data store.
   *
   * @param key The blob's key.
   * @throws FailedDatastoreOperationException Writing to the data store failed.
   * @since 0.1
   */
  public void remove(final @NotNull String key) throws FailedDatastoreOperationException {
    put(key, null);
  }

  /**
   * Wipe out all blobs in the data store.
   *
   * @throws FailedDatastoreOperationException Writing to the data store failed.
   * @since 0.1
   */
  public void clear() throws FailedDatastoreOperationException {
    try {
      blobTable.clear();
    } catch (IOException e) {
      throw new FailedDatastoreOperationException("Failed to clear data store", e);
    }
  }
}

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

package com.rakuten.tech.mobile.datastore.crypto;

import java.nio.ByteBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * Crypto operations that do not implement any crypto.
 *
 * @since 0.1 {@inheritDoc}
 */
public final class NullCryptoOperations implements CryptoOperations {

  @NotNull
  @Override
  public ByteBuffer encrypt(final @NotNull ByteBuffer message) {
    return deepCopyOf(message);
  }

  @NotNull
  @Override
  public ByteBuffer decrypt(final @NotNull ByteBuffer message) {
    return deepCopyOf(message);
  }

  private ByteBuffer deepCopyOf(final @NotNull ByteBuffer buffer) {
    final byte[] bytes = new byte[buffer.remaining()];
    buffer.duplicate().get(bytes);
    return ByteBuffer.wrap(bytes);
  }
}

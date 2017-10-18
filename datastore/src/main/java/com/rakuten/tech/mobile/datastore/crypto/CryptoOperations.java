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
import java.security.GeneralSecurityException;
import org.jetbrains.annotations.NotNull;

/**
 * Cryptographic operations.
 *
 * <p>Simple interface for encrypting and decrypting content.
 *
 * @since 0.1
 */
public interface CryptoOperations {

  /**
   * Encrypt a block of data.
   *
   * @param message Data to encrypt.
   * @return Encrypted date.
   * @throws GeneralSecurityException Encryption failed.
   * @since 0.1
   */
  @NotNull
  ByteBuffer encrypt(final @NotNull ByteBuffer message) throws GeneralSecurityException;

  /**
   * Decrypt a block of data.
   *
   * @param message Data to decrypt.
   * @return Decrypted data.
   * @throws GeneralSecurityException Decryption failed.
   * @since 0.1
   */
  @NotNull
  ByteBuffer decrypt(final @NotNull ByteBuffer message) throws GeneralSecurityException;
}

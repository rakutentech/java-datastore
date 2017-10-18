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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Crypto operations implementing encrypt-then-mac with AES/CBC/PKCS7Padding and HmacSHA256.
 *
 * @since 0.1 {@inheritDoc}
 */
@SuppressFBWarnings("CIPHER_INTEGRITY")
public class SimpleCryptoOperations implements CryptoOperations {

  private static final int SIGNATURE_LENGTH = 32;
  private static final int IV_LENGTH = 16;

  private final Random random;
  private final Key encryptionKey;
  private final Key signingKey;

  /**
   * Create a new instance using a default {@link Random}.
   *
   * @param encryptionKey Key to use for encryption/decryption.
   * @param signingKey Key to use for signing/verifying.
   * @since 0.1
   */
  public SimpleCryptoOperations(
      final @NotNull Key encryptionKey,
      final @NotNull Key signingKey) {
    this(encryptionKey, signingKey, new SecureRandom());
  }

  /**
   * Create a new instance.
   *
   * @param encryptionKey Key to use for encryption/decryption.
   * @param signingKey Key to use for signing/verifying.
   * @param random If {@code null}, {@link #encrypt(ByteBuffer)} will not specify an {@link
   * IvParameterSpec} when calling {@link Cipher#init(int, Key)}. This is useful if the provider
   * automatically manages IVs, such as e.g. the {@code AndroidKeyStore} provider.
   * @since 0.1
   */
  public SimpleCryptoOperations(
      final @NotNull Key encryptionKey,
      final @NotNull Key signingKey,
      final @Nullable Random random) {

    this.encryptionKey = encryptionKey;
    this.signingKey = signingKey;
    this.random = random;
  }

  @NotNull
  protected Mac createNewMacInstance() throws GeneralSecurityException {
    final Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(signingKey);
    return mac;
  }

  @NotNull
  @SuppressFBWarnings("STATIC_IV")
  protected Cipher createNewCipherInstance(final int mode, final @Nullable ByteBuffer presetIv)
      throws GeneralSecurityException {
    final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
    if (presetIv == null) {
      cipher.init(mode, encryptionKey);
    } else {

      cipher.init(mode, encryptionKey, new IvParameterSpec(
          presetIv.array(), presetIv.arrayOffset() + presetIv.position(), presetIv.remaining()));
    }
    return cipher;
  }

  @NotNull
  @Override
  public ByteBuffer encrypt(final @NotNull ByteBuffer message) throws GeneralSecurityException {
    ByteBuffer iv = null;
    if (random != null) {
      iv = ByteBuffer.allocate(IV_LENGTH);
      random.nextBytes(iv.array());
    }

    final Cipher cipher = createNewCipherInstance(Cipher.ENCRYPT_MODE, iv);
    ByteBuffer buffer = ByteBuffer.allocate(
        SIGNATURE_LENGTH + IV_LENGTH + cipher.getOutputSize(message.remaining()));

    // Copy IV
    buffer.position(SIGNATURE_LENGTH);
    buffer.put(cipher.getIV());

    // Encrypt
    int size = SIGNATURE_LENGTH + IV_LENGTH + cipher.doFinal(message.slice(), buffer);
    buffer.rewind();
    buffer.limit(size);

    // Sign IV and message, and prepend signature
    final Mac mac = createNewMacInstance();
    buffer.position(SIGNATURE_LENGTH);
    mac.update(buffer);
    mac.doFinal(buffer.array(), 0);

    buffer.rewind();
    return buffer.slice();
  }

  @NotNull
  @Override
  public ByteBuffer decrypt(final @NotNull ByteBuffer message) throws GeneralSecurityException {
    // Grab a slice for the signature
    ByteBuffer signature = message.slice();
    signature.limit(SIGNATURE_LENGTH);
    signature = signature.slice();

    // …and a slice for the IV
    ByteBuffer iv = message.slice();
    iv.position(SIGNATURE_LENGTH);
    iv.limit(iv.position() + IV_LENGTH);
    iv = iv.slice();

    // …and one for the encrypted content
    ByteBuffer encrypted = message.slice();
    encrypted.position(SIGNATURE_LENGTH + IV_LENGTH);
    encrypted = encrypted.slice();

    // Verify signature
    final Mac mac = createNewMacInstance();
    mac.update(iv);
    mac.update(encrypted);

    final ByteBuffer expected = ByteBuffer.wrap(mac.doFinal());
    if (signature.compareTo(expected) != 0) {
      throw new SignatureException("Signature mismatch");
    }

    // Decrypt
    iv.rewind();
    encrypted.rewind();
    final Cipher cipher = createNewCipherInstance(Cipher.DECRYPT_MODE, iv);
    ByteBuffer buffer = ByteBuffer.allocate(cipher.getOutputSize(encrypted.remaining()));
    int size = cipher.doFinal(encrypted, buffer);
    buffer.rewind();
    buffer.limit(size);

    return buffer.slice();
  }
}

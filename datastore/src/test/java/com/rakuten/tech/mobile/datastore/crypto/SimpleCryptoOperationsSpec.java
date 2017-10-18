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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.util.Random;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("fast")
@DisplayName("Using SimpleCryptoOperations")
class SimpleCryptoOperationsSpec {

  // Register BouncyCastle provider
  static {
    Security.insertProviderAt(new BouncyCastleProvider(), 1);
  }

  // Fixed key bytes
  private static final byte[] KEY_BYTES = new byte[]{
      (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,
      (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,
      (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,
      (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,
  };

  // Fixed random number generator
  private static final Random RANDOM = new Random() {
    @Override
    protected int next(int ignored) {
      return 0;
    }
  };

  private static final SimpleCryptoOperations SUBJECT = new SimpleCryptoOperations(
      new SecretKeySpec(KEY_BYTES, "AES"),
      new SecretKeySpec(KEY_BYTES, "HmacSHA256"),
      RANDOM
  );

  @Nested
  @DisplayName("When encrypting a buffer")
  class WhenEncrypting {

    @Nested
    @DisplayName("If the buffer is empty")
    class IfBufferEmpty {

      @Test
      @DisplayName("It returns something")
      void returnsSomething() throws GeneralSecurityException {
        assertThat(SUBJECT.encrypt(ByteBuffer.allocate(0))).isNotNull();
      }

      @Test
      @DisplayName("The result has the right size")
      void resultHasRightSize() throws GeneralSecurityException {
        // For no content we get 1 AES packet (16 bytes), the IV (16 bytes) and the signature (32 bytes)
        assertThat(SUBJECT.encrypt(ByteBuffer.allocate(0)).remaining()).isEqualTo(16 + 16 + 32);
      }
    }

    @Nested
    @DisplayName("If the buffer is not empty")
    class IfBufferNotEmpty {

      @Test
      @DisplayName("It returns something")
      void returnsSomething() throws GeneralSecurityException {
        assertThat(SUBJECT.encrypt(ByteBuffer.allocate(10))).isNotNull();
      }

      @Test
      @DisplayName("It returns a different instance")
      void returnsDifferentInstances() throws GeneralSecurityException {
        final ByteBuffer original = ByteBuffer.allocate(10);
        assertThat(SUBJECT.encrypt(original)).isNotSameAs(original);
      }

      @Test
      @DisplayName("It returns a buffer with a different backing array instance")
      void returnsDifferentArrayInstances() throws GeneralSecurityException {
        final ByteBuffer original = ByteBuffer.allocate(10);
        assertThat(SUBJECT.encrypt(original).array()).isNotSameAs(original.array());
      }

      @Test
      @DisplayName("The original buffer is not mutated")
      void doesNotMutate() throws GeneralSecurityException {
        final ByteBuffer original = ByteBuffer.allocate(100);
        final int position = 12;
        final int limit = 48;

        original.position(position);
        original.limit(limit);

        SUBJECT.encrypt(original);

        assertThat(original.position()).isEqualTo(position);
        assertThat(original.limit()).isEqualTo(limit);
      }
    }
  }

  @Nested
  @DisplayName("When decrypting a buffer")
  class WhenDecrypting {

    @Nested
    @DisplayName("If the buffer is too short to contain an encrypted message")
    class IfBufferTooShort {

      @Test
      @DisplayName("It throws the right exception")
      void itThrows() throws GeneralSecurityException {
        assertThrows(IllegalArgumentException.class, () -> SUBJECT.decrypt(ByteBuffer.allocate(0)));
      }
    }

    @Nested
    @DisplayName("If the buffer has an invalid signature")
    class IfBufferInvalid {

      @Test
      @DisplayName("It throws the right exception")
      void itThrows() throws GeneralSecurityException {
        // 64 bytes is the smallest size for an encrypted buffer: 32 (sig) + 16 (iv) + 16 (AES packet)
        final ByteBuffer invalid = ByteBuffer.allocate(64);
        new SecureRandom().nextBytes(invalid.array());

        assertThrows(SignatureException.class, () -> SUBJECT.decrypt(invalid));
      }
    }

    @Nested
    @DisplayName("If the buffer is valid")
    class IfBufferValid {

      private final ByteBuffer original = ByteBuffer
          .wrap("Hello World!".getBytes(StandardCharsets.UTF_8));

      @Test
      @DisplayName("It returns something")
      void returnsSomething() throws GeneralSecurityException {
        final ByteBuffer encrypted = SUBJECT.encrypt(original);
        assertThat(SUBJECT.decrypt(encrypted)).isNotNull();
      }

      @Test
      @DisplayName("It returns a different instance")
      void returnsDifferentInstances() throws GeneralSecurityException {
        final ByteBuffer encrypted = SUBJECT.encrypt(original);
        assertThat(SUBJECT.decrypt(encrypted)).isNotSameAs(encrypted);
      }

      @Test
      @DisplayName("It returns a buffer with a different backing array instance")
      void returnsDifferentArrayInstances() throws GeneralSecurityException {
        final ByteBuffer encrypted = SUBJECT.encrypt(original);
        assertThat(SUBJECT.decrypt(encrypted).array()).isNotSameAs(encrypted.array());
      }

      @Test
      @DisplayName("The original buffer is not mutated")
      void doesNotMutate() throws GeneralSecurityException {
        // Wrap the encrypted buffer so that it's padded on both sides
        final ByteBuffer encrypted = SUBJECT.encrypt(original);
        final int length = encrypted.remaining();
        final byte[] array = new byte[length + 100];
        final ByteBuffer wrapped = ByteBuffer.wrap(array);
        encrypted.get(array, 50, length);

        final int position = 50;
        final int limit = position + length;

        wrapped.position(position);
        wrapped.limit(limit);

        SUBJECT.decrypt(wrapped);

        assertThat(wrapped.position()).isEqualTo(position);
        assertThat(wrapped.limit()).isEqualTo(limit);
      }

      @Test
      @DisplayName("It returns a buffer with bytes equal to the original")
      void returnsEqualArrays() throws GeneralSecurityException {
        final ByteBuffer encrypted = SUBJECT.encrypt(original);
        final ByteBuffer decrypted = SUBJECT.decrypt(encrypted);
        assertThat(decrypted).isEqualByComparingTo(original);
      }
    }
  }
}

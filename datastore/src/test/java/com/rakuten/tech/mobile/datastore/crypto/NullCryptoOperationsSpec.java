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

import java.nio.ByteBuffer;
import java.security.SecureRandom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("fast")
@DisplayName("Using NullCryptoOperations")
class NullCryptoOperationsSpec {

  private static final NullCryptoOperations SUBJECT = new NullCryptoOperations();

  @Nested
  @DisplayName("When encrypting a buffer")
  class WhenEncrypting {

    @Test
    @DisplayName("It returns a different instance")
    void returnsDifferentInstances() {
      final ByteBuffer original = ByteBuffer.allocate(10);
      assertThat(SUBJECT.encrypt(original)).isNotNull().isNotSameAs(original);
    }

    @Test
    @DisplayName("It returns a buffer with a different backing array instance")
    void returnsDifferentArrayInstances() {
      final ByteBuffer original = ByteBuffer.allocate(10);
      assertThat(SUBJECT.encrypt(original).array()).isNotSameAs(original.array());
    }

    @Test
    @DisplayName("It produces the same content that was fed to it")
    void doesNotEncrypt() {
      final ByteBuffer original = ByteBuffer.allocate(10);
      new SecureRandom().nextBytes(original.array());
      assertThat(SUBJECT.encrypt(original)).isEqualTo(original);
    }

    @Test
    @DisplayName("The original buffer is not mutated")
    void doesNotMutate() {
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

  @Nested
  @DisplayName("When decrypting a buffer")
  class WhenDecrypting {

    @Test
    @DisplayName("It returns a different instance")
    void returnsDifferentInstances() {
      final ByteBuffer encrypted = ByteBuffer.allocate(10);
      assertThat(SUBJECT.decrypt(encrypted)).isNotNull().isNotSameAs(encrypted);
    }


    @Test
    @DisplayName("It returns a buffer with a different backing array instance")
    void returnsDifferentArrayInstances() {
      final ByteBuffer encrypted = ByteBuffer.allocate(10);
      assertThat(SUBJECT.decrypt(encrypted).array()).isNotSameAs(encrypted.array());
    }

    @Test
    @DisplayName("It produces the same content that was fed to it")
    void doesNotDecrypt() {
      final ByteBuffer original = ByteBuffer.allocate(10);
      new SecureRandom().nextBytes(original.array());
      assertThat(SUBJECT.decrypt(original)).isEqualTo(original);
    }

    @Test
    @DisplayName("The original buffer is not mutated")
    void doesNotMutate() {
      final ByteBuffer original = ByteBuffer.allocate(100);
      final int position = 12;
      final int limit = 48;

      original.position(position);
      original.limit(limit);

      SUBJECT.decrypt(original);

      assertThat(original.position()).isEqualTo(position);
      assertThat(original.limit()).isEqualTo(limit);
    }
  }
}

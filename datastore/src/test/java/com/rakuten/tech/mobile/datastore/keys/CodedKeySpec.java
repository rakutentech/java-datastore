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

package com.rakuten.tech.mobile.datastore.keys;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Using CodedKey")
class CodedKeySpec {

  private static final byte[] BYTES = new byte[]{0, 1, 2, 3, 4, 5, 6, 7};

  @Test
  @DisplayName("It is immutable")
  void bytesAreImmutable() {
    final CodedKey key = new CodedKey(BYTES);

    // Get the key bytes and modify them
    final ByteBuffer alteredBytes = ByteBuffer.wrap(key.getBytes());
    new SecureRandom().nextBytes(alteredBytes.array());

    // Get the key bytes again
    final ByteBuffer immutableBytes = ByteBuffer.wrap(key.getBytes());

    assertThat(alteredBytes).isNotEqualTo(immutableBytes);
  }

  @Nested
  @DisplayName("When testing equality")
  class Equality {

    @Test
    @DisplayName("Keys with equal bytes are equal")
    void equality() {
      final byte[] COPIED_BYTES = Arrays.copyOf(BYTES, BYTES.length);
      assertThat(new CodedKey(COPIED_BYTES)).isEqualTo(new CodedKey(BYTES));
    }

    @Test
    @DisplayName("Keys with different bytes are not equal")
    void inequality() {
      final byte[] COPIED_BYTES = Arrays.copyOf(BYTES, BYTES.length);
      COPIED_BYTES[0] ^= 0x3f;
      assertThat(new CodedKey(COPIED_BYTES)).isNotEqualTo(new CodedKey(BYTES));
    }

    @Test
    @DisplayName("Equal keys have equal hash values")
    void hash() {
      final byte[] COPIED_BYTES = Arrays.copyOf(BYTES, BYTES.length);
      assertThat(new CodedKey(COPIED_BYTES).hashCode()).isEqualTo(new CodedKey(BYTES).hashCode());
    }
  }

  @Nested
  @DisplayName("When comparing")
  class Comparison {

    @Test
    @DisplayName("Keys with equal bytes are equal")
    void compareToEqual() {
      final byte[] COPIED_BYTES = Arrays.copyOf(BYTES, BYTES.length);
      assertThat(new CodedKey(COPIED_BYTES)).isEqualByComparingTo(new CodedKey(BYTES));
    }

    @Test
    @DisplayName("Keys with different bytes are not equal")
    void compareToNotEqual() {
      final byte[] COPIED_BYTES = Arrays.copyOf(BYTES, BYTES.length);
      COPIED_BYTES[0] ^= 0x3f;
      assertThat(new CodedKey(COPIED_BYTES)).isNotEqualByComparingTo(new CodedKey(BYTES));
    }
  }
}

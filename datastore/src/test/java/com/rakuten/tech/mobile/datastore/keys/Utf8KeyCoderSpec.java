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
import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Using Utf8KeyCoder")
class Utf8KeyCoderSpec {

  private static final Utf8KeyCoder SUBJECT = new Utf8KeyCoder();

  @DisplayName("When encoding keys")
  @ParameterizedTest(name = "It properly encodes \"{0}\"")
  @ValueSource(strings = {
      "",
      "😀",
      "こんにちは！",
      "مرحبا"
  })
  void encodes(final @NotNull String value) {
    final ByteBuffer actual = ByteBuffer.wrap(SUBJECT.encode(value).getBytes());
    final ByteBuffer expected = ByteBuffer.wrap(value.getBytes(StandardCharsets.UTF_8));
    assertThat(actual).isEqualTo(expected);
  }
}

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
import java.security.GeneralSecurityException;
import java.util.stream.Stream;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Using Sha256KeyCoder")
class Sha256KeyCoderSpec {

  private static final Sha256KeyCoder SUBJECT = new Sha256KeyCoder();

  @DisplayName("When encoding keys")
  @ParameterizedTest(name = "It properly encodes \"{0}\"")
  @MethodSource("createValueExpectationPairs")
  void encodes(final @NotNull String value, final @NotNull String expectation)
      throws GeneralSecurityException, DecoderException {

    assertThat(Hex.encodeHexString(SUBJECT.encode(value).getBytes())).isEqualTo(expectation);
  }

  private static Stream<Arguments> createValueExpectationPairs() {
    return Stream.of(
        Arguments.of(
            "",
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"),

        Arguments.of(
            "😀",
            "f0443a342c5ef54783a111b51ba56c938e474c32324d90c3a60c9c8e3a37e2d9"),

        Arguments.of(
            "こんにちは！",
            "4427d0b0e72940e3caa881c7b21e48136cbd002b7dcd8c991a38686fabe39929"),

        Arguments.of(
            "مرحبا"
            , "80eff1a750bb540045622ad23c148c8875790515e3f768c77d5dff8c1d221b49")
    );
  }
}

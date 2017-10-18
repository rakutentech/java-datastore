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

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import org.jetbrains.annotations.NotNull;

/**
 * Simple key coder producing a SHA-256 digest of the clear-text key.
 *
 * @since 0.1 {@inheritDoc}
 */
public final class Sha256KeyCoder implements KeyCoder {

  @Override
  public @NotNull CodedKey encode(final @NotNull String clearKey) throws GeneralSecurityException {
    return new CodedKey(
        MessageDigest.getInstance("SHA-256").digest(clearKey.getBytes(StandardCharsets.UTF_8)));
  }
}

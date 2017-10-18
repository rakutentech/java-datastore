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

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import javax.crypto.KeyGenerator;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("PMD.UseUtilityClass")
public class AndroidKeyStoreSimpleCryptoOperations extends SimpleCryptoOperations {

  private static final KeyStore KEY_STORE;

  public static boolean isAvailable() {
    return KEY_STORE != null;
  }

  public AndroidKeyStoreSimpleCryptoOperations(
      final @NotNull String encryptionKeyAlias,
      final @NotNull String signingKeyAlias) throws GeneralSecurityException {

    super(
        getEncryptionKey(encryptionKeyAlias),
        getSigningKey(signingKeyAlias),
        null);
  }

  private static Key getEncryptionKey(final @NotNull String alias) throws GeneralSecurityException {
    return getKey(
        KeyProperties.KEY_ALGORITHM_AES,
        new KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setUserAuthenticationRequired(false)
            .build());
  }

  private static Key getSigningKey(final @NotNull String alias) throws GeneralSecurityException {
    return getKey(
        KeyProperties.KEY_ALGORITHM_HMAC_SHA256,
        new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_SIGN).build());
  }

  // FIXME: For some reason, PMD thinks the method is not used…
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private static Key getKey(
      final @NotNull String algorithm,
      final @NotNull KeyGenParameterSpec spec) throws GeneralSecurityException {

    final String alias = spec.getKeystoreAlias();

    if (!KEY_STORE.containsAlias(alias)) {
      final KeyGenerator kg = KeyGenerator.getInstance(algorithm, "AndroidKeyStore");
      kg.init(spec);
      kg.generateKey();
    }

    Key key = ((KeyStore.SecretKeyEntry) KEY_STORE.getEntry(alias, null)).getSecretKey();

    if (key == null) {
      throw new KeyStoreException("Key disappeared for alias = " + alias);
    }

    return key;
  }

  static {
    KeyStore keyStore;
    try {
      keyStore = KeyStore.getInstance("AndroidKeyStore");
      keyStore.load(null);
    } catch (Exception e) {
      keyStore = null;
    }

    KEY_STORE = keyStore;
  }
}

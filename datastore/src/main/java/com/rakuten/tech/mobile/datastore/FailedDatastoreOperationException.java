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

package com.rakuten.tech.mobile.datastore;

/**
 * Signals that a {@link DataStore} operation failed.
 *
 * {@inheritDoc}
 *
 * @since 0.1
 */
public final class FailedDatastoreOperationException extends Exception {

  /**
   * Constructs a {@code FailedDatastoreOperationException} with the specified detail message and
   * cause.
   *
   * @param message The detail message (which is saved for later retrieval by the {@link
   * Throwable#getMessage()} method).
   * @param throwable The cause (which is saved for later retrieval by the {@link
   * Throwable#getCause()} method). A {@code }null} value is permitted, and indicates that the cause
   * is nonexistent or unknown.
   * @since 0.1
   */
  public FailedDatastoreOperationException(final String message, final Throwable throwable) {
    super(message, throwable);
  }
}

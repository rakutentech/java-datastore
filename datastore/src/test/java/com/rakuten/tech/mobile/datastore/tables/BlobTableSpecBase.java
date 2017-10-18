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

package com.rakuten.tech.mobile.datastore.tables;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumingThat;

import com.rakuten.tech.mobile.datastore.keys.CodedKey;
import com.rakuten.tech.mobile.datastore.keys.Utf8KeyCoder;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import java.util.Iterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BlobTableSpecBase {

  private static final CodedKey KEY = new Utf8KeyCoder().encode("foo");

  private BlobTable table;

  void setTable(final BlobTable table) {
    this.table = table;
  }

  @Nested
  @DisplayName("If the table does not exist yet")
  class TableNotFound {

    @Test
    @DisplayName("Requesting an iterator does not throw")
    void iterator() throws IOException {
      table.iterator();
    }

    @Test
    @DisplayName("Clearing does not throw")
    void clear() throws IOException {
      table.clear();
    }

    @Test
    @DisplayName("Requesting a value does not throw")
    void get() throws IOException {
      assertThat(table.get(KEY)).isNull();
    }

    @Test
    @DisplayName("Probing for a value does not throw")
    void contains() throws IOException {
      assertThat(table.contains(KEY)).isFalse();
    }

    @Nested
    @DisplayName("Using the iterator")
    class WithIterator {

      private Iterator<CodedKey> iterator;

      @BeforeEach
      void beforeEach() throws IOException {
        iterator = table.iterator();
      }

      @Test
      @DisplayName("Yields no element")
      void count() throws IOException {
        assertThat(iterator).isEmpty();
      }
    }
  }

  @Nested
  @DisplayName("After a value is stored")
  class AfterStored {

    @BeforeEach
    void beforeEach() throws IOException {
      table.put(KEY, ByteBuffer.wrap("😀".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("It can be retrieved")
    void get() throws IOException {
      assertThat(table.get(KEY)).isNotNull();
    }

    @Test
    @DisplayName("The retrieved value matches")
    void verify() throws IOException {
      final ByteBuffer value = table.get(KEY);
      assumingThat(value != null, () -> {
        final String actual = new String(value.array(), StandardCharsets.UTF_8);
        assertThat(actual).isEqualTo("😀");
      });
    }

    @Test
    @DisplayName("Probing for it succeeds")
    void contains() throws IOException {
      assertThat(table.contains(KEY)).isTrue();
    }

    @Nested
    @DisplayName("Using the iterator")
    class WithIterator {

      private Iterator<CodedKey> iterator;

      @BeforeEach
      void beforeEach() throws IOException {
        iterator = table.iterator();
      }

      @Test
      @DisplayName("Yields only 1 element")
      void count() throws IOException {
        assertThat(iterator).hasSize(1);
      }

      @Test
      @DisplayName("Yields the value")
      void match() throws IOException {
        assumingThat(iterator.hasNext(), () -> assertThat(iterator.next()).isEqualTo(KEY));
      }

      @Test
      @DisplayName("The value can be removed from the table")
      void remove() throws IOException {
        assumingThat(iterator.hasNext(), () -> {
          iterator.next();
          iterator.remove();

          assertThat(table.get(KEY)).isNull();
        });
      }
    }

    @Nested
    @DisplayName("After the value is deleted")
    class AfterDeleted {

      @BeforeEach
      void beforeEach() throws IOException {
        table.remove(KEY);
      }

      @Test
      @DisplayName("It cannot be retrieved anymore")
      void get() throws IOException {
        assertThat(table.get(KEY)).isNull();
      }

      @Test
      @DisplayName("Probing for it fails")
      void contains() throws IOException {
        assertThat(table.contains(KEY)).isFalse();
      }

      @Nested
      @DisplayName("Using the iterator")
      class WithIterator {

        private Iterator<CodedKey> iterator;

        @BeforeEach
        void beforeEach() throws IOException {
          iterator = table.iterator();
        }

        @Test
        @DisplayName("Yields no element")
        void count() throws IOException {
          assertThat(iterator).isEmpty();
        }
      }
    }

    @Nested
    @DisplayName("After the table is cleared")
    class AfterCleared {

      @BeforeEach
      void beforeEach() throws IOException {
        table.clear();
      }

      @Test
      @DisplayName("The value cannot be retrieved anymore")
      void get() throws IOException {
        assertThat(table.get(KEY)).isNull();
      }

      @Test
      @DisplayName("Probing for the value fails")
      void contains() throws IOException {
        assertThat(table.contains(KEY)).isFalse();
      }

      @Nested
      @DisplayName("Using the iterator")
      class WithIterator {

        private Iterator<CodedKey> iterator;

        @BeforeEach
        void beforeEach() throws IOException {
          iterator = table.iterator();
        }

        @Test
        @DisplayName("Yields no element")
        void count() throws IOException {
          assertThat(iterator).isEmpty();
        }
      }
    }
  }
}

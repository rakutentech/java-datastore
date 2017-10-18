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

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

@DisplayName("Using FileBlobTable")
class FileBlobTableSpec extends BlobTableSpecBase {

  private File temporaryFolder;

  @BeforeEach
  void beforeEach() throws IOException {
    final String name = "tests-" + UUID.randomUUID() + "-" + Timestamp.valueOf(LocalDateTime.now());
    temporaryFolder = new File(FileUtils.getTempDirectory(), name);
    FileUtils.deleteDirectory(temporaryFolder);
    setTable(new FileBlobTable("default", temporaryFolder));
  }

  @AfterEach
  void afterEach() throws IOException {
    setTable(null);
    FileUtils.deleteDirectory(temporaryFolder);
  }
}

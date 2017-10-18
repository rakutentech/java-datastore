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

import com.rakuten.tech.mobile.datastore.keys.CodedKey;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link BlobTable} backed by files.
 *
 * <p>Each instance manages a directory at {@code &lt;rootDirectory&gt;/&lt;name&gt;.blobs/}, where
 * it maintains each blob as a binary file.
 *
 * <p>Calling code is responsible for passing keys that can be used as file names, i.e. that do not
 * contain any special character such as {@code \}, {@code /} or {@code *}.
 *
 * <p>Data managed by instances of this class is persistent.
 *
 * <p>Because of the blocking I/O operations involved, it is recommended that client code uses this
 * class from a background thread.
 *
 * @since 0.1 {@inheritDoc}
 */
@SuppressFBWarnings({
    "IOI_USE_OF_FILE_STREAM_CONSTRUCTORS",
    "PATH_TRAVERSAL_IN",
})
public class FileBlobTable implements BlobTable {

  private static final @NotNull Pattern BAD_BLOB_FILE_NAME = Pattern.compile("^\\..+$");
  private static final @NotNull String OPERATION_NOT_PERMITTED = "Operation not permitted";
  private final @NotNull FilenameFilter blobFilter;
  private final @NotNull File directory;

  /**
   * Construct a new instance backed by a directory.
   *
   * <p>If there is no blob table at {@code &lt;rootDirectory/name&gt;} yet, it will be created upon
   * calling {@link #put(CodedKey, ByteBuffer)}.
   *
   * @param name Name of the blob store to encode under {@code rootDirectory}.
   * @param rootDirectory Where to encode the blob store.
   * @since 0.1
   */
  public FileBlobTable(final @NotNull String name, final @NotNull File rootDirectory) {
    directory = new File(rootDirectory, name);
    blobFilter = new FilenameFilter() {
      @Override
      public boolean accept(final @NotNull File file, @NotNull final String s) {
        return file.equals(directory) && !BAD_BLOB_FILE_NAME.matcher(s).matches();
      }
    };
  }

  @Override
  public boolean contains(final @NotNull CodedKey codedKey) throws IOException {
    try {
      final File file = new File(directory, getBasenameForKey(codedKey));
      return file.exists() && file.canRead();
    } catch (SecurityException e) {
      throw new IOException(OPERATION_NOT_PERMITTED, e);
    }
  }

  @Nullable
  @Override
  public ByteBuffer get(final @NotNull CodedKey codedKey) throws IOException {
    FileInputStream is = null;
    try {
      final File file = new File(directory, getBasenameForKey(codedKey));
      if (!file.canRead()) {
        return null;
      }

      is = new FileInputStream(file);

      final int length = (int) file.length();
      byte[] bytes = new byte[length];
      int offset = 0;
      while (offset < length) {
        int l = is.read(bytes, offset, length - offset);
        if (l == -1) {
          break;
        }
        offset += l;
      }

      return ByteBuffer.wrap(bytes);
    } catch (FileNotFoundException e) {
      return null;

    } catch (SecurityException e) {
      throw new IOException(OPERATION_NOT_PERMITTED, e);

    } finally {
      quietlyClose(is);
    }
  }

  @Override
  public void put(
      final @NotNull CodedKey codedKey,
      final @Nullable ByteBuffer blob) throws IOException {

    if (blob == null) {
      remove(codedKey);
      return;
    }

    checkCreateDirectory();

    FileOutputStream out = null;
    try {
      final File tmpFile = new File(directory, "." + getBasenameForKey(codedKey) + ".tmp");
      if (!tmpFile.exists() && !tmpFile.createNewFile()) {
        throw new IOException("Could not encode file at " + tmpFile.getAbsolutePath());
      }

      out = new FileOutputStream(tmpFile);
      FileChannel channel = out.getChannel();

      ByteBuffer slice = blob.slice();
      int remaining = slice.remaining();
      while (remaining > 0) {
        int written = channel.write(slice);
        if (written > 0) {
          remaining -= written;
        }
      }

      quietlyClose(out);

      remove(codedKey);
      final File file = new File(directory, getBasenameForKey(codedKey));
      if (!tmpFile.renameTo(file)) {
        throw new IOException(String.format("Could not move file %s to %s",
            tmpFile.getAbsolutePath(),
            file.getAbsolutePath()));
      }
    } catch (SecurityException e) {
      throw new IOException(OPERATION_NOT_PERMITTED, e);

    } finally {
      quietlyClose(out);
    }
  }

  @Override
  public void remove(final @NotNull CodedKey codedKey) throws IOException {
    try {
      final File file = new File(directory, getBasenameForKey(codedKey));
      if (file.exists() && !file.delete()) {
        throw new IOException("Could not remove file at " + file.getAbsolutePath());
      }
    } catch (SecurityException e) {
      throw new IOException(OPERATION_NOT_PERMITTED, e);
    }
  }

  @Override
  public void clear() throws IOException {
    /*
     * Try to clear everything that can be. The first exception encountered is kept until all
     * entries have been processed, then rethrown at the end.
     */
    IOException error = null;

    for (CodedKey codedKey : getAllKeys()) {
      try {
        remove(codedKey);
      } catch (IOException e) {
        if (error == null) {
          error = e;
        }
      }
    }

    if (error != null) {
      throw error;
    }
  }

  @NotNull
  @Override
  public Iterator<CodedKey> iterator() throws IOException {
    return BlobTableIterator.of(this, getAllKeys().iterator());
  }

  private void checkCreateDirectory() throws IOException {
    try {
      if (!directory.exists() && !directory.mkdirs()) {
        throw new IOException("Could not encode directory at " + directory.getAbsolutePath());
      }
    } catch (SecurityException e) {
      throw new IOException(OPERATION_NOT_PERMITTED, e);
    }
  }

  private void quietlyClose(final Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (Exception ignored) {
        // no-op
      }
    }
  }

  private List<CodedKey> getAllKeys() throws IOException {
    try {
      final String[] nameArray = directory.list(blobFilter);

      if (nameArray == null) {
        return Collections.emptyList();
      } else {
        List<CodedKey> codedKeys = new ArrayList<>(nameArray.length);
        for (String basename : nameArray) {
          codedKeys.add(getKeyForBasename(basename));
        }
        return codedKeys;
      }
    } catch (SecurityException e) {
      throw new IOException(OPERATION_NOT_PERMITTED, e);
    }
  }

  private String getBasenameForKey(final @NotNull CodedKey codedKey) {
    return Base64.encodeBase64URLSafeString(codedKey.getBytes());
  }

  @NotNull
  private CodedKey getKeyForBasename(final @NotNull String basename) {
    return new CodedKey(Base64.decodeBase64(basename));
  }
}

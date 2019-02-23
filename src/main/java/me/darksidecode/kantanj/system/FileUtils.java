/*
 * Copyright 2019 DarksideCode
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

package me.darksidecode.kantanj.system;

import me.darksidecode.kantanj.crypto.Encryptor;
import me.darksidecode.kantanj.types.Check;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class FileUtils {

    private FileUtils() {}

    public static byte[] readGZIP(File gzipFile) {
        Check.fileExists(gzipFile, "no such file %s", gzipFile.getAbsolutePath());

        try (FileInputStream fin = new FileInputStream(gzipFile);
             GZIPInputStream gzipIn = new GZIPInputStream(fin)) {
            return IOUtils.toByteArray(gzipIn);
        } catch (Exception ex) {
            throw new RuntimeException("failed to " +
                    "read gzip file " + gzipFile.getAbsolutePath(), ex);
        }
    }

    public static byte[] readEncryptedGZIP(File gzipFile, Encryptor encryptor) {
        Check.fileExists(gzipFile, "no such file %s", gzipFile.getAbsolutePath());
        Check.notNull(encryptor, "encryptor cannot be null");

        File decrypted = new File(gzipFile.
                getAbsolutePath() + ".decr" + System.nanoTime());
        decrypted.deleteOnExit();

        try {
            byte[] origData = Files.readAllBytes(gzipFile.toPath());
            byte[] decryptedData = encryptor.decrypt(origData);

            Files.write(decrypted.toPath(), decryptedData);

            return readGZIP(decrypted);
        } catch (Exception ex) {
            throw new RuntimeException("failed to " +
                    "read encrypted gzip file " + gzipFile.getAbsolutePath(), ex);
        } finally {
            //noinspection ResultOfMethodCallIgnored
            decrypted.delete();
        }
    }

    public static void writeGZIP(File file, byte[] data, OverwriteMode overwriteMode) {
        Check.notNull(file, "file cannot be null");
        Check.notNull(data, "data cannot be null");
        Check.notNull(overwriteMode, "overwriteMode cannot be null");

        if (file.exists()) {
            if (file.isDirectory())
                throw new IllegalArgumentException(
                        "expected file, got directory: " + file.getAbsolutePath());

            switch (overwriteMode) {
                default:
                    throw new UnsupportedOperationException(
                            "unsupported overwrite mode: " + overwriteMode.name());

                case THROW_STATE_EXCEPTION:
                    throw new IllegalStateException("file already exists: " + file.getAbsolutePath());

                case DO_NOT_WRITE:
                    return;

                case OVERWRITE:
                    // Delete and proceed.
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                    break;

                case BACKUP:
                    String origPath = file.getAbsolutePath();

                    if (origPath.endsWith("/"))
                        origPath = origPath.substring(0, origPath.length() - 1);

                    try {
                        // Create a backup with name "{orig_name}.{cur_time_millis}~"
                        byte[] originalData = Files.readAllBytes(file.toPath());
                        Files.write(new File(origPath + "."
                                + System.currentTimeMillis() + "~").toPath(), originalData);

                        // Delete and proceed.
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    } catch (IOException ex) {
                        throw new RuntimeException("failed to backup file " + origPath, ex);
                    }

                    break;
            }
        }

        try (FileOutputStream fos = new FileOutputStream(file);
             GZIPOutputStream gzipOut = new GZIPOutputStream(fos)) {
            gzipOut.write(data);
        } catch (Exception ex) {
            throw new RuntimeException("failed to write gzip file " +
                    "(overwrite mode: " + overwriteMode + ") " + file.getAbsolutePath(), ex);
        }
    }

    public static void writeEncryptedGZIP(File gzipFile, byte[] data,
                                          Encryptor encryptor, OverwriteMode overwriteMode) {
        Check.notNull(encryptor, "encryptor cannot be null");
        writeGZIP(gzipFile, data, overwriteMode);

        File encrypted = new File(gzipFile.
                getAbsolutePath() + ".encr" + System.nanoTime());
        encrypted.deleteOnExit();

        try {
            Path origPath = gzipFile.toPath();
            Path encryptedPath = encrypted.toPath();

            byte[] origData = Files.readAllBytes(origPath);
            byte[] encryptedData = encryptor.encrypt(origData);

            Files.write(encryptedPath, encryptedData);
            Files.move(encryptedPath, origPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ex) {
            throw new RuntimeException("failed to write encrypted gzip file " +
                    "(overwrite mode: " + overwriteMode + ") " + gzipFile.getAbsolutePath(), ex);
        } finally {
            //noinspection ResultOfMethodCallIgnored
            encrypted.delete();
        }
    }

    public enum OverwriteMode {
        THROW_STATE_EXCEPTION,

        DO_NOT_WRITE,

        OVERWRITE,

        BACKUP,

        ;
    }


}

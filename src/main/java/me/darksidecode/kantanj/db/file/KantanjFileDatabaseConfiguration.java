/*
 * Copyright 2021 DarksideCode
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

package me.darksidecode.kantanj.db.file;

import lombok.Getter;
import me.darksidecode.kantanj.crypto.AES256Encryptor;
import me.darksidecode.kantanj.crypto.Encryptor;
import me.darksidecode.kantanj.db.DatabaseConfiguration;
import me.darksidecode.kantanj.types.Check;

import java.io.File;
import java.nio.file.Path;

@Getter
public class KantanjFileDatabaseConfiguration implements DatabaseConfiguration {

    /**
     * The file the database is contained it.
     */
    private File sourceFile;

    /**
     * Save database entries in file instantly after insert/update or not.
     * If not, then saves will only be done on database exit/disconnect or
     * on an entry expiration.
     */
    private boolean flushInstantly;

    /**
     * Save database entries automatically upon JVM exit (using shutdown hooks) or not.
     */
    private boolean flushOnExit;

    /**
     * Authentication and encryption mechanism used for this database.
     * May be null - in that case, the database will be treated as
     * unsecured and no sort of encryption will be performed for read/write
     * operations.
     */
    private Encryptor encryptor;

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private static final byte[] ENC_SALT = { -128, 62, 60, 10, -11, -101, -70, -112 };

        private final KantanjFileDatabaseConfiguration conf = new KantanjFileDatabaseConfiguration();

        private int aesIterations = AES256Encryptor.DEFAULT_ITERATIONS;

        Builder() {}

        public KantanjFileDatabaseConfiguration build() {
            return conf;
        }

        public Builder file(String path) {
            return file(new File(path));
        }

        public Builder file(Path path) {
            return file(path.toFile());
        }

        public Builder file(File sourceFile) {
            conf.sourceFile = Check.notNull(sourceFile, "sourceFile cannot be null");
            return this;
        }

        public Builder flushInstantly(boolean flushInstantly) {
            conf.flushInstantly = flushInstantly;
            return this;
        }

        public Builder flushOnExit(boolean flushOnExit) {
            conf.flushOnExit = flushOnExit;
            return this;
        }

        public Builder aesIterations(int aesIterations) {
            if (aesIterations < 1)
                throw new IllegalArgumentException("must iterate at least once");

            Check.state(conf.encryptor != null, "encryptor " +
                    "already set (aesIterations must be set before password)");

            this.aesIterations = aesIterations;
            return this;
        }

        public Builder password(char[] password) {
            conf.encryptor = new AES256Encryptor(password, ENC_SALT, aesIterations);
            return this;
        }

        public Builder encryptor(Encryptor encryptor) {
            conf.encryptor = encryptor;
            return this;
        }
    }

}

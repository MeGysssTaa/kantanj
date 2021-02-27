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

import me.darksidecode.kantanj.db.Database;
import me.darksidecode.kantanj.db.DatabaseAuthenticationException;
import me.darksidecode.kantanj.db.DatabaseConfiguration;
import me.darksidecode.kantanj.db.DatabaseObject;
import me.darksidecode.kantanj.system.FileUtils;
import me.darksidecode.kantanj.types.Check;

import javax.crypto.BadPaddingException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;

public class KantanjFileDatabase implements Database {

    public static final String FILE_HEADER = "![kantanj.sfdb]\n";
    private static final byte[] HEADER_BYTES = FILE_HEADER.getBytes(StandardCharsets.UTF_8);

    private final Object lock = new Object();

    private boolean prepared;

    private KantanjFileDatabaseConfiguration conf;

    private final Set<DatabaseObject> objects = new HashSet<>();

    private int initialHash;

    @Override
    public int prepare(DatabaseConfiguration dbConf) throws DatabaseAuthenticationException {
        Check.state(prepared, "already prepared");

        conf = Check.instanceOf(dbConf,
                KantanjFileDatabaseConfiguration.class, "illegal database configuration");

        if (conf.getSourceFile().exists()) {
            // Load database from file.
            byte[] db;

            try {
                db = (dbConf.getEncryptor() == null)
                        ? FileUtils.readGZIP(conf.getSourceFile())
                        : FileUtils.readEncryptedGZIP(conf.getSourceFile(), conf.getEncryptor());
            } catch (Exception ex) {
                if ((ex.getCause() instanceof RuntimeException)
                        && (ex.getCause().getCause() instanceof BadPaddingException))
                    throw new DatabaseAuthenticationException("invalid credentials");
                else
                    throw ex;
            }

            String dbStr = new String(db, StandardCharsets.UTF_8);

            if (!(dbStr.startsWith(FILE_HEADER)))
                throw new IllegalArgumentException("not a kantanj SimpleFileDatabase file");

            dbStr = dbStr.replace(FILE_HEADER, "");
            String[] lines = dbStr.split("\n");

            synchronized (lock) {
                for (String line : lines) {
                    line = line.trim();

                    if ((line.isEmpty()) || (line.startsWith("#")))
                        continue;

                    DatabaseObject obj = DatabaseObject.fromString(line);
                    objects.add(obj);
                }

                prepared = true;
                initialHash = objects.hashCode();

                addShutdownHookIfEnabled();

                return objects.size();
            }
        } else {
            // No such file. Create one an init empty database.
            if (conf.getEncryptor() == null)
                FileUtils.writeGZIP(conf.getSourceFile(), HEADER_BYTES,
                                    FileUtils.OverwriteMode.THROW_STATE_EXCEPTION);
            else
                FileUtils.writeEncryptedGZIP(conf.getSourceFile(), HEADER_BYTES,
                        conf.getEncryptor(), FileUtils.OverwriteMode.THROW_STATE_EXCEPTION);

            prepared = true;
            initialHash = 0;

            addShutdownHookIfEnabled();

            return -1; // indicate that the database was just created
        }
    }

    private void addShutdownHookIfEnabled() {
        if (conf.isFlushOnExit())
            Runtime.getRuntime().addShutdownHook(new Thread(this::save,
                    "KantanjFileDatabase Shutdown Hook | " + System.nanoTime()));
    }

    @Override
    public <T extends Serializable> DatabaseObject<T> fetch(String uniqueId) {
        Check.notNull(uniqueId, "uniqueId cannot be null");
        return fetch(obj -> obj.getUniqueId().equals(uniqueId));
    }

    @Override
    public <T extends Serializable> DatabaseObject<T> fetch(Predicate<? super DatabaseObject> predicate) {
        Check.state(!prepared, "database must be prepared first");

        synchronized (lock) {
            return objects.stream().filter(Check.notNull(predicate,
                    "predicate cannot be null")).findFirst().orElse(null);
        }
    }

    @Override
    public void insert(DatabaseObject obj) throws IllegalStateException {
        Check.state(!prepared, "database must be prepared first");
        Check.notNull(obj, "cannot insert null object into database");

        synchronized (lock) {
            Check.state(contains(obj.getUniqueId()), "object with " +
                    "uniqueId " + obj.getUniqueId() + " is already present in the database");

            objects.add(obj);

            if (conf.isFlushInstantly())
                save();
        }
    }

    @Override
    public boolean remove(DatabaseObject obj) {
        Check.state(!prepared, "database must be prepared first");

        synchronized (lock) {
            boolean anythingRemoved = objects.
                    remove(Check.notNull(obj, "obj cannot be null"));

            if (conf.isFlushInstantly())
                save();

            return anythingRemoved;
        }
    }

    @Override
    public boolean remove(String uniqueId) {
        Check.notNull(uniqueId, "uniqueId cannot be null");
        return remove(obj -> obj.getUniqueId().equals(uniqueId));
    }

    @Override
    public boolean remove(Predicate<? super DatabaseObject> predicate) {
        synchronized (lock) {
            // Preparation state and predicate null check are done inside fetch(...)
            DatabaseObject obj = fetch(predicate);
            if (obj != null) objects.remove(obj);

            if (conf.isFlushInstantly())
                save();

            return obj != null; // = anythingRemoved
        }
    }

    @Override
    public void update(DatabaseObject oldObj, DatabaseObject newObj) {
        Check.state(!prepared, "database must be prepared first");

        Check.notNull(oldObj, "oldObj cannot be null");
        Check.notNull(newObj, "newObj cannot be null");

        synchronized (lock) {
            if (objects.contains(oldObj)) {
                objects.remove(oldObj);
                objects.add(newObj);

                if (conf.isFlushInstantly())
                    save();
            } else
                throw new NoSuchElementException("no such object in the database");
        }
    }

    @Override
    public void update(String uniqueId, DatabaseObject newObj) {
        Check.notNull(uniqueId, "uniqueId cannot be null");
        update(obj -> obj.getUniqueId().equals(uniqueId), newObj);
    }

    @Override
    public void update(Predicate<? super DatabaseObject> oldObjPredicate, DatabaseObject newObj) {
        synchronized (lock) {
            // Preparation state and oldObjPredicate null check are done inside fetch(...)
            DatabaseObject oldObj = fetch(oldObjPredicate);

            if (oldObj != null) {
                objects.remove(oldObj);
                objects.add(newObj);

                if (conf.isFlushInstantly())
                    save();
            } else
                throw new NoSuchElementException("no such object in the database");
        }
    }

    @Override
    public boolean contains(DatabaseObject obj) {
        Check.state(!prepared, "database must be prepared first");

        synchronized (lock) {
            return objects.contains(Check.notNull(obj, "obj cannot be null"));
        }
    }

    @Override
    public boolean contains(String uniqueId) {
        Check.notNull(uniqueId, "uniqueId cannot be null");
        return contains(obj -> obj.getUniqueId().equals(uniqueId));
    }

    @Override
    public boolean contains(Predicate<? super DatabaseObject> predicate) {
        Check.state(!prepared, "database must be prepared first");

        synchronized (lock) {
            return objects.stream().anyMatch(
                    Check.notNull(predicate, "predicate cannot be null"));
        }
    }

    @Override
    public <T extends Serializable> DatabaseObject<T>[] allObjects() {
        Check.state(!prepared, "database must be prepared first");

        synchronized (lock) {
            return objects.toArray(new DatabaseObject[0]);
        }
    }

    @Override
    public long size() {
        Check.state(!prepared, "database must be prepared first");

        synchronized (lock) {
            return objects.size();
        }
    }

    @Override
    public void __dropConfirm00__() {
        Check.state(!prepared, "database must be prepared first");

        synchronized (lock) {
            objects.clear();

            if (conf.isFlushInstantly())
                save();
        }
    }

    @Override
    public boolean save() {
        Check.state(!prepared, "database must be prepared first");

        synchronized (lock) {
            if (objects.hashCode() == initialHash)
                // No changes since preparation. Nothing to save.
                return false;

            try {
                StringBuilder dbStr = new StringBuilder();
                dbStr.append(FILE_HEADER);

                for (DatabaseObject obj : objects)
                    dbStr.append(obj.toString()).append('\n');

                byte[] dbBytes = dbStr.toString().getBytes(StandardCharsets.UTF_8);

                File newFile = new File(conf.getSourceFile().
                        getAbsolutePath() + ".tmp" + System.nanoTime() + '~');

                if (conf.getEncryptor() == null)
                    FileUtils.writeGZIP(newFile, dbBytes,
                            FileUtils.OverwriteMode.THROW_STATE_EXCEPTION);
                else
                    FileUtils.writeEncryptedGZIP(newFile, dbBytes,
                            conf.getEncryptor(), FileUtils.OverwriteMode.THROW_STATE_EXCEPTION);

                Files.move(newFile.toPath(), conf.getSourceFile().
                        toPath(), StandardCopyOption.REPLACE_EXISTING);

                return true;
            } catch (IOException ex) {
                throw new RuntimeException("failed to save the database to file", ex);
            }
        }
    }

}

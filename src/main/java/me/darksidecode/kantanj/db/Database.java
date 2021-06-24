/*
 * Copyright 2021 German Vekhorev (DarksideCode)
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

package me.darksidecode.kantanj.db;

import java.io.Serializable;
import java.util.function.Predicate;

public interface Database {

    /**
     * @return the number of database objects loaded, if the database already
     *         existed and was successfuly loaded, or -1 if the database did
     *         not exist on the moment of this call. -1 indicates that a new
     *         empty database with specified options was just created.
     *
     * @throws DatabaseAuthenticationException in case of a database authentication
     *                                         failure caused by, for example, inputting
     *                                         invalid credentials.
     */
    int prepare(DatabaseConfiguration dbConf) throws DatabaseAuthenticationException;

    <T extends Serializable> DatabaseObject<T> fetch(String uniqueId);

    <T extends Serializable> DatabaseObject<T> fetch(Predicate<? super DatabaseObject> predicate);

    /**
     * @throws IllegalStateException if there is already an object with the same uniqueId
     *                               as in the specified one in the database.
     */
    void insert(DatabaseObject obj) throws IllegalStateException;

    /**
     * @return true if and only if the remove operation succeeded, i.e. at least
     *         one database element was removing during it.
     */
    boolean remove(DatabaseObject obj);

    /**
     * @return true if and only if the remove operation succeeded, i.e. at least
     *         one database element was removing during it.
     */
    boolean remove(String uniqueId);


    /**
     * @return true if and only if the remove operation succeeded, i.e. at least
     *         one database element was removing during it.
     */
    boolean remove(Predicate<? super DatabaseObject> predicate);

    void update(DatabaseObject oldObj, DatabaseObject newObj);

    /**
     * NOTE: the uniqueId of the specified new object must
     *       be equal to the specified `uniqueId` parameter.
     */
    void update(String uniqueId, DatabaseObject newObj);

    void update(Predicate<? super DatabaseObject> oldObjPredicate, DatabaseObject newObj);

    boolean contains(DatabaseObject obj);

    boolean contains(String uniqueId);

    boolean contains(Predicate<? super DatabaseObject> predicate);

    <T extends Serializable> DatabaseObject<T>[] allObjects();

    long size();

    /**
     * Delete all entries from this database.
     * The action may not be possible to undone.
     *
     * NOTE: this method always throws a DropConfirmationException, which must
     *       be handled and confirmed using DropConfirmationException#confirmAndDrop,
     *       otherwise the call to this method will be ignored.
     *
     * This method should not be normally overriden.
     *
     * @throws DropConfirmationException see above.
     */
    default void drop() throws DropConfirmationException {
        throw new DropConfirmationException(this);
    }

    /**
     * Must not be called manually.
     * Only used internally by DropConfirmationException.
     *
     * @see Database#drop()
     */
    void __dropConfirm00__();

    /**
     * @return true if and only if the database was saved successfully.
     *         A result of false, however, does not have to be an error.
     *         It may also indicate that there are no changes since last
     *         preparation, which means there is nothing to save.
     */
    boolean save();

}

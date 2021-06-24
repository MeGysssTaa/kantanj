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

package me.darksidecode.kantanj.db.mongo;

import org.bson.Document;

/**
 * Basic cacheable database object skeleton.
 *
 * Objects implementing this interface must be
 * override all the methods for complete compliance
 * with database data caching.
 *
 * A basic Cacheable object allows to (1) fetch its
 * **existing** instance from a specified database;
 * (2) insert its instance into a specified database;
 * and (3) keep track of all changes made to its
 * mutable values and push them to a specified database
 * (if any).
 */
public interface Cacheable {

    /**
     * Fill this object's fields with values fetched from the
     * specified database or throw a NoSuchElementException if
     * it cannot be found there.
     *
     * @param database database to fetch data from.
     * @param uniqueIdentifier object identifier value unique for each object.
     *                         The identifier field name ("key") fully depends
     *                         upon implementation; same goes for allowed types
     *                         for uniqueIdentifier values. For instance, for
     *                         user objects the implementation may require this
     *                         parameter to be a String in order to compare it
     *                         to types String values in database under key named,
     *                         say, "username". Some implementations may allow
     *                         uniqueIdentifier to be null.
     *
     * @throws java.util.NoSuchElementException if there are no elements in the specified
     *                                          database "id-keys" of which have a value
     *                                          equal to the specified uniqueIdentifier
     *                                          (no entries with the specified identifier found).
     */
    void fetch(MongoManager database, Object uniqueIdentifier);

    /**
     * Insert this object in a database, if not there yet.
     *
     * Actually, no direct insertion is performed. Instead,
     * a BSON/Document object is returned with all the necessary
     * data for insertion (unique ("primary") key and types entries
     * that objects of this type should store in a database).
     *
     * The responsibility for insertion of the returned object
     * fully lies on caller.
     *
     * @throws IllegalStateException if this object has not been initialized yet.
     *                               Primarily, initialization is done using the
     *                               fetch(MongoManager, Object) method. However,
     *                               additional ways to perform it may exist depending
     *                               upon implementation.
     *
     * @return a BSON/Document that should be inserted in a database
     *         and that will represent this object as a database object.
     */
    Document create();

    /**
     * Push changes made to this object's mutable fields since last fetch/creation.
     *
     * Actually, no direct insertion is performed. Instead,
     * a BSON/Document object is returned with all the data
     * that needs to be updated, if any, or null otherwise.
     *
     * The responsibility for insertion of the returned object
     * fully lies on caller. In case null is returned, simply no
     * insertion should be performed, no errors should be thrown.
     *
     * @throws IllegalStateException if this object has not been initialized yet.
     *                               Primarily, initialization is done using the
     *                               fetch(MongoManager, Object) method. However,
     *                               additional ways to perform it may exist depending
     *                               upon implementation.
     *
     * @return a BSON/Document that should be inserted in a database
     *         **using the $set option** to save updated values in a
     *         database, if any, or null otherwise, that should be
     *         ignored as stated above.
     */
    Document push();

}

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

package me.darksidecode.kantanj.db.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import me.darksidecode.kantanj.types.Check;
import org.bson.Document;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoManager implements AutoCloseable {

    private static final int DEFAULT_MONGO_PORT = 27017;

    private MongoClient client;
    private MongoDatabase selected;

    public MongoManager noLogs() {
        Logger.getLogger("com.mongodb").setLevel(Level.OFF);
        Logger.getLogger("org.mongodb").setLevel(Level.OFF);
        
        return this;
    }

    public MongoManager connectLocal() {
        try {
            client = new MongoClient();
            return this;
        } catch (Exception ex) {
            throw new RuntimeException("failed to connect to default local database", ex);
        }
    }

    public MongoManager connectLocal(String username, String database, char[] password) {
        try {
            return connect("localhost", DEFAULT_MONGO_PORT, username, database, password);
        } catch (Exception ex) {
            throw new RuntimeException("failed to connect to default local database", ex);
        }
    }

    public MongoManager connect(String host) {
        if ((host == null) || (host.trim().isEmpty()))
            throw new IllegalArgumentException("invalid (null, empty or 'spaces-only') host");

        try {
            client = new MongoClient(host);
            return this;
        } catch (Exception ex) {
            throw new RuntimeException("failed to connect " +
                    "to remote database \"" + host + "\" (default port)", ex);
        }
    }

    public MongoManager connect(String host, int port) {
        if ((host == null) || (host.trim().isEmpty()))
            throw new IllegalArgumentException("invalid (null, empty or 'spaces-only') host");

        if ((port < 1) || (port > 65535))
            throw new IllegalArgumentException("invalid port " + port);

        try {
            client = new MongoClient(host, port);
            return this;
        } catch (Exception ex) {
            throw new RuntimeException(String.format("failed to " +
                    "connect to remote database \"%s\" at port %s", host, port));
        }
    }

    public MongoManager connect(String host, int port, String username, String database, char[] password) {
        if ((host == null) || (host.trim().isEmpty()))
            throw new IllegalArgumentException("invalid (null, empty or 'spaces-only') host");

        if ((username == null) || (username.trim().isEmpty()))
            throw new IllegalArgumentException("invalid (null, empty or 'spaces-only') username");

        if ((database == null) || (database.trim().isEmpty()))
            throw new IllegalArgumentException("invalid (null, empty or 'spaces-only') database");

        if ((password == null) || (password.length == 0))
            throw new IllegalArgumentException("invalid (null or empty) password");

        if ((port < 1) || (port > 65535))
            throw new IllegalArgumentException("invalid port " + port);

        try {
            MongoCredential cred = MongoCredential.createCredential(username, database, password);

            // Clear original password array
            for (int i = 0; i < password.length; i++)
                password[i] = (char) 0;

            client = new MongoClient(new ServerAddress(host, port), cred, MongoClientOptions.builder().build());
            // deprecated api usage: client = new MongoClient(new ServerAddress(host, port), Collections.singletonList(cred));

            return this;
        } catch (Exception ex) {
            throw new RuntimeException("failed to connect to default local database (with user&pass)", ex);
        }
    }

    public MongoManager select(String database) {
        ensureConnected();
        selected = client.getDatabase(Check.
                notNull(database, "database name cannot be null"));
        return this;
    }

    public MongoDatabase getSelectedDatabase() {
        ensureSelected();
        return selected;
    }

    public boolean isDatabaseSelected() {
        ensureConnected();
        return selected != null;
    }

    public boolean isConnected() {
        return client != null;
    }

    private void ensureSelected() {
        ensureConnected();

        if (!(isDatabaseSelected()))
            throw new IllegalStateException("no database selected");
    }

    private void ensureConnected() {
        if (!(isConnected()))
            throw new IllegalStateException("not connected");
    }

    @Override
    public void close() {
        if (!(isConnected()))
            return;

        try {
            client.close();
        } catch (Exception ignored) {}
    }

    private <T> T checkStateAndReturn(T object) {
        ensureSelected();
        return object;
    }

    public MongoCollection<Document> getCollection(String collection) {
        return checkStateAndReturn(
                selected.getCollection(Check.
                        notNull(collection, "collection name cannot be null")));
    }

    public FindIterable<Document> fetch(String collection, String key, Object val) {
        // val IS allowed to be null;
        // getCollection ensures that collection!=null
        //               and that a connection is open and a database is selected.
        return getCollection(collection).find(new Document(
                Check.notNull(key, "key cannot be null"), val));
    }

    public Document fetchFirst(String collection, String key, Object val) {
        // Parameter types and state checks are done inside the base fetch(...) method
        return fetch(collection, key, val).first();
    }

    public void insertOne(String collection, Document doc) {
        // getCollection ensures collection!=null and checks state
        getCollection(collection).insertOne(
                Check.notNull(doc, "doc cannot be null"));
    }

    public void insertMany(String collection, List<Document> docs) {
        if (Check.notNull(docs, "docs list cannot be null").isEmpty())
            throw new IllegalArgumentException("nothing to insert");

        // getCollection ensures collection!=null and checks state
        MongoCollection<Document> col = getCollection(collection);

        if (docs.size() == 1)
            col.insertOne(docs.get(0));
        else col.insertMany(docs);
    }

    public Document updateOne(String collection, String idKey, Object idVal, Document updatedData) {
        Check.notNull(idKey, "target entry ID key cannot be null");
        Check.notNull(updatedData, "updated data cannot be null");

        // getCollection ensures collection!=null and checks state
        MongoCollection<Document> col = getCollection(collection);
        Document targetEntry = col.find(new Document(idKey, idVal)).first();

        Check.notNull(targetEntry, "no entries with \"%s\"=\"%s\" found", idKey, idVal);
        col.updateOne(targetEntry, new Document("$set", updatedData));

        return targetEntry;
    }

    public long deleteOne(String collection, String idKey, Object idVal) {
        Check.notNull(idKey, "target entry ID key cannot be null");

        // getCollection ensures collection!=null and checks state
        MongoCollection<Document> col = getCollection(collection);
        return col.deleteOne(new Document(idKey, idVal)).getDeletedCount();
    }

    public long count(String collection) {
        // getCollection ensures collection!=null and checks state
        return getCollection(collection).countDocuments();
    }

}

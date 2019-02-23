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

package me.darksidecode.kantanj.db;

import com.google.gson.internal.LinkedTreeMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.darksidecode.kantanj.formatting.CommonJson;
import me.darksidecode.kantanj.formatting.Formatting;
import me.darksidecode.kantanj.types.Check;
import org.bson.Document;

import java.io.Serializable;

@AllArgsConstructor
public class DatabaseObject<T extends Serializable> implements Serializable, Cloneable {

    private static final long serialVersionUID = 6177749799489717775L;

    private static final String DB_OBJ_LINE = "\u0000";

    @Getter
    private String uniqueId;

    @Getter
    private T value;

    @Override
    public String toString() {
        String json = Formatting.unixLines(CommonJson.toJson(this));

        if (json.contains(DB_OBJ_LINE))
            throw new UnknownError("json contains invalid null chars");

        return json.
                replace("\n", DB_OBJ_LINE);
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = Check.notNull(uniqueId, "uniqueId cannot be null");
    }

    public void setValue(T value) {
        this.value = Check.notNull(value, "value cannot be null");
    }

    @SuppressWarnings ("MethodDoesntCallSuperMethod")
    public DatabaseObject clone() {
        return new DatabaseObject(uniqueId, value);
    }

    public static <T extends Serializable> DatabaseObject<T> fromString(String dbJson) {
        if (dbJson.contains("\n"))
            throw new IllegalArgumentException("json contains illegal non-db line chars");

        DatabaseObject<T> obj = CommonJson.fromJson(dbJson.
                replace(DB_OBJ_LINE, "\n"), DatabaseObject.class);

        if ((obj.getValue() instanceof LinkedTreeMap)
                && (GlobalDatabasesOptions.isConvertLinkedTreeMapToBsonDocument())) {
            // Try to convert this LinkedTreeMap to a BSON Document object.
            try {
                obj.setValue((T) new Document((LinkedTreeMap) obj.getValue()));
            } catch (ClassCastException ex) {
                throw new RuntimeException("failed to convert a LinkedTreeMap to a BSON Document " +
                        "(unexpected T generic type); see me.darksidecode.kantanj.db." +
                        "GlobalDatabasesOptions#convertLinkedTreeMapToBsonDocument", ex);
            }
        }

        return obj;
    }

}

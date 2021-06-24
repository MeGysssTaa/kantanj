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

package me.darksidecode.kantanj.types;

import java.util.Map;

public final class Maps {

    private Maps() {}

    public static <K, V> void copy(Map<K, V> src, Map<K, V> dest) {
        Check.notNull(src, "source map cannot be null");
        Check.notNull(dest, "destination map cannot be null");

        src.forEach(dest::put);
    }

    public static <K, V> Map<K, V> copy(Map<K, V> src, Class<? extends Map> typeOfDestMap) {
        Check.notNull(src, "source map cannot be null");
        Check.notNull(typeOfDestMap, "type of destination map cannot be null");

        Map<K, V> dest;

        try {
            dest = typeOfDestMap.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException("failed to instantiate" +
                    "a new Map of type " + typeOfDestMap.getName(), ex);
        }

        src.forEach(dest::put);

        return dest;
    }

}

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

import java.util.NoSuchElementException;
import java.util.Optional;

public final class Env {

    private Env() {}

    public static Optional<Integer> getVarInteger(String key) {
        Optional<String> o = getVarString(key);

        if (o.isPresent()) {
            try {
                return Optional.of(Integer.parseInt(o.get()));
            } catch (NumberFormatException ex) {
                throw new NumberFormatException("env \"" + key + "\" is not of type Integer");
            }
        }

        return Optional.empty();
    }

    public static Integer requireVarInteger(String key) {
        return getVarInteger(key).orElseThrow(()
                -> new NoSuchElementException("no such Integer env specified: " + key));
    }

    public static Optional<Short> getVarShort(String key) {
        Optional<String> o = getVarString(key);

        if (o.isPresent()) {
            try {
                return Optional.of(Short.parseShort(o.get()));
            } catch (NumberFormatException ex) {
                throw new NumberFormatException("env \"" + key + "\" is not of type Short");
            }
        }

        return Optional.empty();
    }

    public static Short requireVarShort(String key) {
        return getVarShort(key).orElseThrow(()
                -> new NoSuchElementException("no such Short env specified: " + key));
    }

    public static Optional<Long> getVarLong(String key) {
        Optional<String> o = getVarString(key);

        if (o.isPresent()) {
            try {
                return Optional.of(Long.parseLong(o.get()));
            } catch (NumberFormatException ex) {
                throw new NumberFormatException("env \"" + key + "\" is not of type Long");
            }
        }

        return Optional.empty();
    }

    public static Long requireVarLong(String key) {
        return getVarLong(key).orElseThrow(()
                -> new NoSuchElementException("no such Long env specified: " + key));
    }

    public static Optional<Double> getVarDouble(String key) {
        Optional<String> o = getVarString(key);

        if (o.isPresent()) {
            try {
                return Optional.of(Double.parseDouble(o.get()));
            } catch (NumberFormatException ex) {
                throw new NumberFormatException("env \"" + key + "\" is not of type Double");
            }
        }

        return Optional.empty();
    }

    public static Double requireVarDouble(String key) {
        return getVarDouble(key).orElseThrow(()
                -> new NoSuchElementException("no such Double env specified: " + key));
    }

    public static Optional<Float> getVarFloat(String key) {
        Optional<String> o = getVarString(key);

        if (o.isPresent()) {
            try {
                return Optional.of(Float.parseFloat(o.get()));
            } catch (NumberFormatException ex) {
                throw new NumberFormatException("env \"" + key + "\" is not of type Float");
            }
        }

        return Optional.empty();
    }

    public static Float requireVarFloat(String key) {
        return getVarFloat(key).orElseThrow(()
                -> new NoSuchElementException("no such Float env specified: " + key));
    }

    public static Optional<Boolean> getVarBoolean(String key) {
        Optional<String> o = getVarString(key);

        if (o.isPresent()) {
            switch (o.get().toLowerCase()) {
                case "1":
                case "yes":
                case "true":
                    return Optional.of(true);

                case "0":
                case "no":
                case "false":
                    return Optional.of(false);

                default:
                    throw new NumberFormatException("env \"" + key + "\" is not of type Boolean");
            }
        }

        return Optional.empty();
    }
    
    public static Boolean requireVarBoolean(String key) {
        return getVarBoolean(key).orElseThrow(()
                -> new NoSuchElementException("no such Boolean env specified: " + key));
    }

    public static Optional<String> getVarString(String key) {
        return Optional.ofNullable(System.getenv(key));
    }

    public static String requireVarString(String key) {
        return getVarString(key).orElseThrow(()
                -> new NoSuchElementException("no such String env specified: " + key));
    }
    
}

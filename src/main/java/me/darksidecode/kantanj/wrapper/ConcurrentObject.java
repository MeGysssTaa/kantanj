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

package me.darksidecode.kantanj.wrapper;

import lombok.Getter;
import me.darksidecode.kantanj.types.Check;

public class ConcurrentObject<T> {

    @Getter
    private final Object lock;

    private T value;

    public ConcurrentObject() {
        this(new Object(), null);
    }

    public ConcurrentObject(T initialValue) {
        this(new Object(), initialValue);
    }

    public ConcurrentObject(Object lock, T initialValue) {
        this.lock = Check.notNull(lock, "lock cannot be null");
        this.value = initialValue;
    }

    public void set(T newValue) {
        synchronized (lock) {
            value = newValue;
        }
    }

    public T get() {
        synchronized (lock) {
            return value;
        }
    }

    public T getAndSwap(T newValue) {
        synchronized (lock) {
            T oldValue = value;
            value = newValue;

            return oldValue;
        }
    }

}

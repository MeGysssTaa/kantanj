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

package me.darksidecode.kantanj.wrapper;

import java.util.concurrent.TimeUnit;

public abstract class ExpiringObject<T> {

    protected T value;

    private final long lifespanMillis;

    private long lastUpdateTime;

    public ExpiringObject(long lifespan, TimeUnit lifespanUnit) {
        if ((lifespanMillis = lifespanUnit.toMillis(lifespan)) < 1L)
            throw new IllegalArgumentException("lifespan cannot be less than 1 millisecond");
        update();
    }

    public ExpiringObject(T initialValue, long lifespan, TimeUnit lifespanUnit) {
        value = initialValue;

        if ((lifespanMillis = lifespanUnit.toMillis(lifespan)) < 1L)
            throw new IllegalArgumentException("lifespan cannot be less than 1 millisecond");

        lastUpdateTime = System.currentTimeMillis();
    }

    public boolean hasExpired() {
        return System.currentTimeMillis() > lastUpdateTime + lifespanMillis;
    }

    public T get() {
        if (hasExpired())
            update();
        return value;
    }

    public final void update() {
        try {
            update0();
        } catch (Exception ex) {
            throw new RuntimeException("failed to update " + getClass().getName(), ex);
        } finally {
            lastUpdateTime = System.currentTimeMillis();
        }
    }

    protected abstract void update0() throws Exception;

}

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

package me.darksidecode.kantanj.currency;

import lombok.Getter;
import me.darksidecode.kantanj.types.Check;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * List of a few most popular/used currencies.
 * Enum names, number codes, and maybe currency names (the human-readable ones)
 * all follow ISO-4217.
 *
 * See https://www.iban.com/currency-codes
 *
 * TODO: add more currencies
 */
@Getter
@SuppressWarnings ("OctalInteger")
public enum Currency {

    USD ("US Dollar",          (short) 840),

    EUR ("Euro",               (short) 978),

    RUB ("Russian Ruble",      (short) 643),

    CNY ("Yuan Renminbi",      (short) 156),

    GBP ("Pound Sterling",     (short) 826),

    JPY ("Yen",                (short) 392),

    CHF ("Swiss Franc",        (short) 756),

    AUD ("Australian Dollar",  (short) 036),

    CAD ("Canadian Dollar",    (short) 124),

    SEK ("Swedish Krona",      (short) 752),

    NZD ("New Zealand Dollar", (short) 554),

    ;

    private final String name;
    private final short number;

    Currency(String name, short number) {
        this.name = name;
        this.number = number;
    }

    @Override
    public String toString() {
        return name() + '/' + number + " (" + name + ")";
    }

    public static Currency byHumanReadableName(String name) throws NoSuchElementException {
        Check.notNull(name, "currency name cannot be null");
        return Arrays.stream(values()).filter(currency -> currency.getName().equalsIgnoreCase(name)).
                findAny().orElseThrow(() -> new NoSuchElementException("unsupported currency: name: " + name));
    }

    public static Currency byNumber(short number) throws NoSuchElementException {
        if ((number < 0) || (number > 999))
            throw new IllegalArgumentException("currency number must be between 0 and 999 (inclusive)");

        return Arrays.stream(values()).filter(currency -> currency.getNumber() == number).
                findAny().orElseThrow(() -> new NoSuchElementException("unsupported currency: number: " + number));
    }

}

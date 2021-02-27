/*
 * Copyright 2020 DarksideCode
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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.darksidecode.kantanj.networking.GetHttpRequest;
import me.darksidecode.kantanj.networking.Networking;
import me.darksidecode.kantanj.networking.SampleUserAgents;
import me.darksidecode.kantanj.types.Check;

/**
 * A simple implementation of a currency converter based on https://www.currencyconverterapi.com/docs
 */
public class SimpleCurrencyConverter implements CurrencyConverter {

    public static final String FREE_URL = "https://free.currencyconverterapi.com";

    public static final String PREMIUM_URL = "https://api.currencyconverterapi.com";

    /**
     * The latest www.currencyconverterapi.com API version at the moment when
     * this code was being written, March 03 2019.
     */
    public static final String V6 = "v6";

    private final String baseUrl;
    private final String apiKey;

    private String apiVersion = V6;

    /**
     * A convenience constructor to create free converters easier.
     * @param apiKey API key to use for requests on currencyconverterapi.com. Cannot be null.
     */
    public SimpleCurrencyConverter(String apiKey) {
        this(false, apiKey);
    }

    /**
     * Constructs a new simple currency converter based on currencyconverterapi.com.
     *
     * @param premium Whether the API key (see below) is premium (false if it is free).
     * @param apiKey API key to use for requests. Cannot be null.
     */
    public SimpleCurrencyConverter(boolean premium, String apiKey) {
        this.baseUrl = (premium) ? PREMIUM_URL : FREE_URL;
        this.apiKey = Check.notNull(apiKey, "API key cannot be null");
    }

    public SimpleCurrencyConverter apiVersion(String apiVersion) {
        this.apiVersion = Check.notNull(apiVersion, "API version cannot be null");
        return this;
    }

    @Override
    public float convert(float amount, Currency from, Currency to) {
        if (amount < 0.0f)
            throw new IllegalArgumentException("amount cannot be negative");

        if (amount == 0.0f)
            return 0.0f;

        Check.notNull(from, "source currency cannot be null");
        Check.notNull(to,   "target currency cannot be null");

        try {
            String arg = from.name() + '_' + to.name();
            GetHttpRequest getRequest = (GetHttpRequest) new GetHttpRequest().
                    baseUrl(baseUrl).
                    path("api/" + apiVersion + "/convert").
                    queryParams(
                            "q",        arg,
                            "compact", "ultra",
                            "apiKey",   apiKey
                    ).
                    requestProperty("Content-Type", "application/json; charset=UTF-8").
                    userAgent(SampleUserAgents.MOZILLA_WIN_NT);

            String response = Networking.Http.get(getRequest);
            JsonObject json = new JsonParser().parse(response).getAsJsonObject();

            float conversionRate = json.get(arg).getAsFloat();
            return amount * conversionRate;
        } catch (Exception ex) {
            throw new RuntimeException("failed to convert "
                    + amount + " " + from.name() + " to " + to.name(), ex);
        }
    }

}

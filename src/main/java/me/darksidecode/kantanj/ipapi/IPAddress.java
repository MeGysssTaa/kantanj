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

package me.darksidecode.kantanj.ipapi;

import com.google.gson.Gson;
import lombok.Getter;
import me.darksidecode.kantanj.formatting.CommonJson;

import java.io.Serializable;

/**
 * An ip-api.com'S IP JSON information wrapper.
 */
@Getter
public class IPAddress implements Serializable {

    private static final long serialVersionUID = 1423606597409892108L;

    /**
     * Response status - "success" or "fail".
     *
     * EXAMPLE: success
     */
    private String status;

    /**
     * Included only when status is "fail".
     * Can be one of the following: "private range", "reserved range", "invalid query".
     *
     * EXAMPLE: invalid query
     */
    private String message;

    /**
     * Country name.
     *
     * EXAMPLE: United States
     */
    private String country;

    /**
     * Two-letter country code ISO 3166-1 alpha-2.
     *
     * EXAMPLE: US
     */
    private String countryCode;

    /**
     * Region/state short code (FIPS or ISO).
     *
     * EXAMPLES: (1) "CA" or (2) "10"
     */
    private String region;

    /**
     * Region/state.
     *
     * EXAMPLE: California
     */
    private String regionName;

    /**
     * City name.
     *
     * EXAMPLE: Mountain View
     */
    private String city;

    /**
     * District (subdivision of city).
     *
     * EXAMPLE: Old Farm District
     */
    private String district;

    /**
     * Zip code.
     *
     * EXAMPLE: 94043
     */
    private String zip;

    /**
     * Latitude.
     *
     * EXAMPLE: 37.4192
     */
    private float lat;

    /**
     * Longitude.
     *
     * EXAMPLE: -122.0574
     */
    private float lon;

    /**
     * City timezone.
     *
     * EXAMPLE: America/Los_Angeles
     */
    private String timezone;

    /**
     * ISP name.
     *
     * EXAMPLE: Google
     */
    private String isp;

    /**
     * Organization name.
     *
     * EXAMPLE: Google
     */
    private String org;

    /**
     * AS (autonomous system) number and name, separated by space.
     *
     * EXAMPLE: AS15169 Google Inc.
     */
    private String as;

    /**
     * Reverse DNS of the IP.
     *
     * EXAMPLE: wi-in-f94.1e100.net
     */
    private String reverse;

    /**
     * Mobile (cellular) connection.
     *
     * EXAMPLE: true
     */
    private boolean cellular;

    /**
     * Proxy (anonymous).
     *
     * EXAMPLE: true
     */
    private boolean proxy;

    /**
     * IP used for the query.
     *
     * EXAMPLE: 173.194.67.94
     */
    private String query;

    @Override
    public String toString() {
        return toString(CommonJson.getPrettyGsonNoHtmlEsc());
    }

    public String toString(Gson gson) {
        return gson.toJson(this);
    }

}

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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.darksidecode.kantanj.formatting.CommonJson;
import me.darksidecode.kantanj.networking.GetHttpRequest;
import me.darksidecode.kantanj.networking.Networking;
import me.darksidecode.kantanj.networking.SampleUserAgents;
import me.darksidecode.kantanj.system.Threads;
import me.darksidecode.kantanj.time.ExpiringList;
import me.darksidecode.kantanj.types.Check;
import org.apache.http.entity.ContentType;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * An http://ip-api.com IP information API wrapper.
 * Uses JSON.
 */
public class IPAPI {

    public static final String BASE_URL = "http://ip-api.com";

    private static final int MAX_REQUESTS_PER_MIN = 40; // 45 for ip-api

    private static final long MIN_BLOCK_MILLIS = 1000;

    @Getter @Setter
    private static boolean enableCaching = true;

    /**
     * Should the current thread be blocked for `threadBlockMillis` if the number
     * of requests in last minute exceeds the set limit (MAX_REQUESTS_PER_MIN)?
     * This is necessary in order to avoid the requestor IP being banned.
     */
    @Getter @Setter
    private static boolean blockOnLimitExcess = true;

    /**
     * @see IPAPI#blockOnLimitExcess
     */
    private static long threadBlockMillis = TimeUnit.SECONDS.toMillis(30);

    private static final ExpiringList requestCounter = new ExpiringList(1, TimeUnit.MINUTES);

    private static final Cache<Integer, IPAddress> cache =
            CacheBuilder.newBuilder().
                    expireAfterWrite(1, TimeUnit.DAYS). // let the IP data change once a day
                    build();

    private static final Object cacheLock = new Object();
    private static final Object requestLock = new Object();

    public static void setThreadBlockTime(long blockTime, TimeUnit timeUnit) {
        if (blockTime < 1)
            throw new IllegalArgumentException("blockTime cannot be negative or zero");

        long millis = Check.notNull(timeUnit,
                "timeUnit cannot be null").toMillis(blockTime);

        if (millis < MIN_BLOCK_MILLIS)
            throw new IllegalArgumentException("cannot block for less than 1 second");

        threadBlockMillis = millis;
    }

    public static IPAddress info(InetAddress address) {
        return info(address.getHostAddress());
    }

    public static IPAddress info(InetAddress address, IPAPILanguage lang) {
        return info(address.getHostAddress(), lang);
    }

    public static IPAddress info(String ip) {
        return info(ip, IPAPILanguage.ENGLISH);
    }

    public static IPAddress info(String ip, IPAPILanguage lang) {
        if (enableCaching) {
            // Caching of IPAPIRequest objects doesn't work for some reason:
            // cache.get(ipapiRequest) always returns null.
            IPAPIRequest ipapiRequest = new IPAPIRequest(ip, lang);
            int hash = ipapiRequest.hashCode();

            synchronized (cacheLock) {
                IPAddress cached = cache.getIfPresent(hash);

                if (cached != null)
                    return cached;

                IPAddress ipAddr = info0(ip, lang);
                cache.put(hash, ipAddr);

                return ipAddr;
            }
        } else
            return info0(ip, lang);
    }

    private static IPAddress info0(String ip, IPAPILanguage lang) {
        synchronized (requestLock) {
            int requestsLastMin = requestCounter.updateAndCount();

            if ((requestsLastMin > MAX_REQUESTS_PER_MIN) && (blockOnLimitExcess))
                // Block to avoid getting our IP banned.
                Threads.sleepQuietly(threadBlockMillis);

            GetHttpRequest request = (GetHttpRequest) new GetHttpRequest().
                    baseUrl(BASE_URL).
                    path("json/" + ip).
                    queryParam("lang", lang.getIso639Name()).
                    requestProperty("Content-Type", ContentType.APPLICATION_JSON.toString()).
                    userAgent(SampleUserAgents.MOZILLA_WIN_NT);

            String response = Networking.Http.get(request);
            return CommonJson.fromJson(response, IPAddress.class);
        }
    }

    @Getter @RequiredArgsConstructor
    private static class IPAPIRequest {
        private final String ip;
        private final IPAPILanguage lang;

        @Override
        public int hashCode() {
            return ip.hashCode() + lang.name().hashCode();
        }
    }

}

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

package me.darksidecode.kantanj.formatting;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;

public enum Hash {

    MD5    (    "MD5"),
    SHA1   (   "SHA1"),
    SHA256 ("SHA-256"),
    SHA512 ("SHA-512");

    private String name;

    Hash(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public byte[] checksum(File input) {
        try (InputStream in = new FileInputStream(input)) {
            MessageDigest digest = MessageDigest.getInstance(name);

            byte[] block = new byte[4096];
            int length;

            while ((length = in.read(block)) > 0)
                digest.update(block, 0, length);
            return digest.digest();
        } catch (Exception ex) {
            throw new RuntimeException("failed to get checksum of " + input.getAbsolutePath(), ex);
        }
    }

    public String checksumString(File input) {
        return hex(checksum(input));
    }

    public byte[] checksum(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(name);
            digest.update(input);

            return digest.digest();
        } catch (Exception ex) {
            throw new RuntimeException("failed to get checksum of " + input.length + " bytes", ex);
        }
    }

    public String checksumString(byte[] input) {
        return hex(checksum(input));
    }

    public byte[] checksum(String input, Charset encoding) {
        return checksum(input.getBytes(encoding));
    }

    public String checksumString(String input, Charset encoding) {
        return hex(checksum(input, encoding));
    }

    public static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        
        for (byte b : bytes)
            sb.append(String.format("%02x", b));
        
        return sb.toString();
    }

}
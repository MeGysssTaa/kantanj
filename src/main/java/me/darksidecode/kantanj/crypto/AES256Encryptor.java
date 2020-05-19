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

package me.darksidecode.kantanj.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class AES256Encryptor implements Encryptor {

    public static final int DEFAULT_ITERATIONS = 65536;

    private static final String KEYGEN_ALGO = "PBKDF2WithHmacSHA256";
    private static final String CRYPT_ALGO  = "AES/CBC/PKCS5PADDING";

    private final char[] key;
    private final byte[] salt;

    private final int iterations;

    public AES256Encryptor(char[] key, byte[] salt) {
        this(key, salt, DEFAULT_ITERATIONS);
    }

    public AES256Encryptor(char[] key, byte[] salt, int iterations) {
        if ((key == null) || (key.length == 0))
            throw new IllegalArgumentException("key cannot be null or empty");
        if ((salt == null) || (salt.length != 8))
            throw new IllegalArgumentException("Invalid salt. Required length: 8");
        if (iterations < 2)
            throw new IllegalArgumentException("Too few iterations. Expected at least 2");

        this.key = key.clone();
        this.salt = salt;
        this.iterations = iterations;
    }

    private SecretKey getSecret() throws InvalidKeySpecException, NoSuchAlgorithmException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEYGEN_ALGO);
        KeySpec spec = new PBEKeySpec(key, salt, iterations, 256);
        SecretKey secret = factory.generateSecret(spec);

        return new SecretKeySpec(secret.getEncoded(), "AES");
    }

    @Override
    public byte[] encrypt(byte[] input) {
        try {
            Cipher cipher = Cipher.getInstance(CRYPT_ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, getSecret());
            AlgorithmParameters params = cipher.getParameters();

            byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
            byte[] cipherData = cipher.doFinal(input);

            return new CipherObj(cipherData, iv).getBytes();
        } catch (Exception ex) {
            throw new RuntimeException("failed to encrypt " + input.length + " bytes with AES (256)", ex);
        }
    }

    @Override
    public byte[] decrypt(byte[] input) {
        try {
            CipherObj cObj = CipherObj.from(input);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            cipher.init(Cipher.DECRYPT_MODE, getSecret(), new IvParameterSpec(cObj.iv));

            return cipher.doFinal(cObj.cipherData);
        } catch (Exception ex) {
            throw new RuntimeException("failed to decrypt " + input.length + " bytes with AES (256)", ex);
        }
    }

   private static class CipherObj {
        private final byte[] cipherData, iv;

        private CipherObj(byte[] cipherData, byte[] iv) {
            if ((cipherData == null) || (cipherData.length == 0)
                    || ((cipherData.length % 16) != 0))
                throw new RuntimeException("invalid cipherdata");

            if ((iv == null) || (iv.length != 16))
                throw new RuntimeException("invalid init vector");

            this.cipherData = cipherData;
            this.iv = iv;
        }

        private byte[] getBytes() {
            byte[] b = new byte[16 + cipherData.length];

            System.arraycopy(iv, 0, b, 0, 16);
            System.arraycopy(cipherData, 0, b, 16, cipherData.length);

            return b;
        }

        private static CipherObj from(byte[] b) {
            byte[] iv = new byte[16];
            byte[] cipherData = new byte[b.length - 16];

            System.arraycopy(b, 0, iv, 0, 16);
            System.arraycopy(b, 16, cipherData, 0, cipherData.length);

            return new CipherObj(cipherData, iv);
        }
    }

}
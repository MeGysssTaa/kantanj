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

package me.darksidecode.kantanj.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES128Encryptor implements Encryptor {

    private static final String ALGO = "AES/CBC/PKCS5PADDING";

    private final byte[] key, iv;
    
    public AES128Encryptor(byte[] key, byte[] iv) {
        if ((key == null) || (key.length != 16))
            throw new IllegalArgumentException("Invalid key. Required length: 16");
        if ((iv == null) || (iv.length != 16))
            throw new IllegalArgumentException("Invalid init vector. Required length: 16");

        this.key = key;
        this.iv = iv;
    }

    @Override
    public byte[] encrypt(byte[] input) {
        try {
            IvParameterSpec iv = new IvParameterSpec(this.iv);
            SecretKeySpec skc = new SecretKeySpec(key, "AES");

            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.ENCRYPT_MODE, skc, iv);

            return c.doFinal(input);
        } catch (Exception ex) {
            throw new RuntimeException("failed to encrypt " + input.length + " bytes with AES (128)", ex);
        }
    }

    @Override
    public byte[] decrypt(byte[] input) {
        try {
            IvParameterSpec iv = new IvParameterSpec(this.iv);
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            return cipher.doFinal(input);
        } catch (Exception ex) {
            throw new RuntimeException("failed to decrypt " + input.length + " bytes with AES (128)", ex);
        }
    }

}

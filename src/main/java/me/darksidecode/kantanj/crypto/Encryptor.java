package me.darksidecode.kantanj.crypto;

public interface Encryptor {

    byte[] encrypt(final byte[] input);

    byte[] decrypt(final byte[] input);

}

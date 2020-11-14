package net.mckitsu.lib.network.encrypt;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class Encrypt {
    private final Cipher cipherEncrypt;
    private final Cipher cipherDecrypt;

    private Encrypt(Type type) throws NoSuchAlgorithmException {
        try {
            cipherEncrypt = Cipher.getInstance(type.instance);
            cipherDecrypt = Cipher.getInstance(type.instance);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new NoSuchAlgorithmException();
        }
    }

    public Encrypt(Key key, Type type) throws NoSuchAlgorithmException, InvalidKeyException {
        this(type);
        cipherEncrypt.init(Cipher.ENCRYPT_MODE, key);
        cipherDecrypt.init(Cipher.DECRYPT_MODE, key);
    }

    public final byte[] decrypt(byte[] src){
        try {
            return cipherDecrypt.doFinal(src);
        } catch (Exception e) {
            return null;
        }
    }

    public final byte[] encrypt(byte[] src){
        try {
            return cipherEncrypt.doFinal(src);
        } catch (Exception e) {
            return null;
        }
    }

    public enum Type{
        AES("AES"),
        RSA("RSA");

        private final String instance;

        Type(String string){
            this.instance = string;
        }

        public String getInstance() {
            return instance;
        }
    }
}

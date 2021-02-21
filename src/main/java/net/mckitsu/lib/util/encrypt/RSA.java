package net.mckitsu.lib.util.encrypt;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSA extends Encrypt {
    public RSA(byte[] key, KeyType keyType) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        this(keyConvert(key, keyType));
    }

    public RSA(Key key) throws InvalidKeyException, NoSuchAlgorithmException {
        super(key,Type.RSA);
        if(!key.getAlgorithm().equalsIgnoreCase(Type.RSA.getInstance()))
            throw new NoSuchAlgorithmException();
    }

    private static Key keyConvert(byte[] key, KeyType keyType) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory kf = KeyFactory.getInstance("RSA"); // or "EC" or whatever

        switch (keyType){
            case PRIVATE_KEY:
                return kf.generatePrivate(new PKCS8EncodedKeySpec(key));
            case PUBLIC_KEY:
                return kf.generatePublic(new X509EncodedKeySpec(key));
        }
        return null;
    }

    public enum KeyType{
        PRIVATE_KEY,
        PUBLIC_KEY,
    }
}

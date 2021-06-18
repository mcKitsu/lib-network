package net.mckitsu.lib.network.util;

import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class EncryptAes extends Encrypt {
    public EncryptAes(byte[] key) throws NoSuchAlgorithmException, InvalidKeyException {
        this(new SecretKeySpec(key, "AES"));
    }

    public EncryptAes(Key key) throws InvalidKeyException, NoSuchAlgorithmException {
        super(key, Type.AES);
        if(!key.getAlgorithm().equalsIgnoreCase(Type.AES.getInstance()))
            throw new NoSuchAlgorithmException();
    }
}

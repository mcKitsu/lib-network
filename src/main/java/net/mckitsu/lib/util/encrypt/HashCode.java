package net.mckitsu.lib.util.encrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashCode {
    private final MessageDigest messageDigest;

    public HashCode(HashType type) throws NoSuchAlgorithmException {
        messageDigest = MessageDigest.getInstance(type.type);
    }

    public byte[] hash(byte[] input){
        return this.messageDigest.digest(input);
    }

    public enum HashType{
        MD2("MD2"),
        MD5("MD5"),
        SHA1("SHA1"),
        SHA256("SHA-256"),
        SHA384("SHA-384"),
        SHA512("SHA-512");

        HashType(String type){
            this.type = type;
        }

        private final String type;
    }
}

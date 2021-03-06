package net.mckitsu.lib.network.util;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class EncryptRsa extends Encrypt {
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */

    /* **************************************************************************************
     *  Variable <Private>
     */

    /* **************************************************************************************
     *  Abstract method <Public>
     */

    /* **************************************************************************************
     *  Abstract method <Protected>
     */

    /* **************************************************************************************
     *  Construct Method
     */
    public EncryptRsa(byte[] key, KeyType keyType) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        this(keyConvert(key, keyType));
    }


    public EncryptRsa(Key key) throws InvalidKeyException, NoSuchAlgorithmException {
        super(key,Type.RSA);
        if(!key.getAlgorithm().equalsIgnoreCase(Type.RSA.getInstance()))
            throw new NoSuchAlgorithmException();
    }


    /* **************************************************************************************
     *  Public Method
     */

    /* **************************************************************************************
     *  Public Method <Override>
     */

    /* **************************************************************************************
     *  Public Method <Static>
     */

    /* **************************************************************************************
     *  Protected Method
     */

    /* **************************************************************************************
     *  Protected Method <Override>
     */

    /* **************************************************************************************
     *  Protected Method <Static>
     */

    /* **************************************************************************************
     *  Private Method
     */

    /* **************************************************************************************
     *  Private Method <Override>
     */

    /* **************************************************************************************
     *  Private Method <Static>
     */

    /*----------------------------------------
     *  keyConvert
     *----------------------------------------*/
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


    /* **************************************************************************************
     *  Public Enum KeyType
     */
    public enum KeyType{
        PRIVATE_KEY,
        PUBLIC_KEY,
    }
}

package net.mckitsu.lib.network.util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class Encrypt {
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */

    /* **************************************************************************************
     *  Variable <Private>
     */
    private final Cipher cipherEncrypt;
    private final Cipher cipherDecrypt;


    /* **************************************************************************************
     *  Abstract method <Public>
     */

    /* **************************************************************************************
     *  Abstract method <Protected>
     */

    /* **************************************************************************************
     *  Construct Method
     */
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


    /* **************************************************************************************
     *  Public Method
     */

    /*----------------------------------------
     *  decrypt
     *----------------------------------------*/
    public final byte[] decrypt(byte[] src) throws BadPaddingException, IllegalBlockSizeException {
        return cipherDecrypt.doFinal(src);
    }


    /*----------------------------------------
     *  decrypt
     *----------------------------------------*/
    public final byte[] encrypt(byte[] src) throws BadPaddingException, IllegalBlockSizeException {
        return cipherEncrypt.doFinal(src);
    }


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

    /* **************************************************************************************
     *  Public Enum Type
     */
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

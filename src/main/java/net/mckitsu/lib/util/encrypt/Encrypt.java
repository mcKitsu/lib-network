package net.mckitsu.lib.util.encrypt;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.nio.ByteBuffer;
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
    public byte[] decrypt(byte[] input){
        try {
            return cipherDecrypt.doFinal(input);
        } catch (Exception e) {
            return null;
        }
    }

    public int decrypt(ByteBuffer input, ByteBuffer output){
        try {
            return cipherDecrypt.doFinal(input, output);
        } catch (Exception e) {
            return 0;
        }
    }

    public int decrypt(byte[] input, ByteBuffer output){
        return decrypt(ByteBuffer.wrap(input), output);
    }

    public byte[] decrypt(byte[] input, int inputOffset, int inputLen){
        try {
            return cipherDecrypt.doFinal(input, inputOffset, inputLen);
        } catch (Exception e) {
            return null;
        }
    }

    public byte[] encrypt(byte[] src){
        try {
            return cipherEncrypt.doFinal(src);
        } catch (Exception e) {
            return null;
        }
    }

    public int encrypt(ByteBuffer input, ByteBuffer output){
        try {
            return cipherEncrypt.doFinal(input, output);
        } catch (Exception e) {
            return 0;
        }
    }

    public int encrypt(byte[] input, ByteBuffer output){
        return encrypt(ByteBuffer.wrap(input), output);
    }

    public byte[] encrypt(byte[] input, int inputOffset, int inputLen){
        try {
            return cipherEncrypt.doFinal(input, inputOffset, inputLen);
        } catch (Exception e) {
            return null;
        }
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

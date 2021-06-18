package net.mckitsu.lib.network;

import net.mckitsu.lib.network.util.CompletionHandlerEvent;
import net.mckitsu.lib.network.util.EncryptAes;
import net.mckitsu.lib.network.util.EncryptRsa;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class HandshakeMaster extends Handshake {
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */

    /* **************************************************************************************
     *  Variable <Private>
     */
    private final CompletionHandlerEvent<Integer, ByteBuffer> eventWriteRsaPublicKey
            = new CompletionHandlerEvent<>(this::eventWriteRsaPublicKeyCompleted, this::eventFailed);

    private final CompletionHandlerEvent<Integer, ByteBuffer> eventReadAesKey
            = new CompletionHandlerEvent<>(this::eventReadAesKeyCompleted, this::eventFailed);

    private boolean isAction;
    private EncryptAes encryptAes;
    private EncryptRsa encryptRsa;
    private CompletionHandler<EncryptAes, Void> handler;
    private Network network;
    private byte[] rsaPublicKey;



    /* **************************************************************************************
     *  Abstract method <Public>
     */

    /* **************************************************************************************
     *  Abstract method <Protected>
     */

    /* **************************************************************************************
     *  Construct Method
     */
    protected HandshakeMaster(){
        this.isAction = false;
    }

    /* **************************************************************************************
     *  Public Method
     */

    /* **************************************************************************************
     *  Public Method <Override>
     */


    @Override
    public void action(Network network, CompletionHandler<EncryptAes, Void> handler) {
        if(this.isAction){
            handler.failed(new IOException("multi action"), null);
            return;
        }

        try {
            this.isAction = true;
            this.handler = handler;
            this.network = network;

            KeyPair keyPair = generatorRsaKey();
            this.encryptRsa = new EncryptRsa(keyPair.getPrivate());
            this.rsaPublicKey = keyPair.getPublic().getEncoded();

            ByteBuffer byteBufferRsaPublicKey = ByteBuffer.wrap(this.rsaPublicKey);
            this.network.directWrite(byteBufferRsaPublicKey, byteBufferRsaPublicKey, this.eventWriteRsaPublicKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            this.isAction = false;
            super.doFailed(handler, e);
        }
    }



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
    private KeyPair generatorRsaKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(512);
        return keyPairGenerator.generateKeyPair();
    }

    public void eventWriteRsaPublicKeyCompleted(Integer result, ByteBuffer attachment) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(16384);
        this.network.directRead(byteBuffer, byteBuffer, this.eventReadAesKey);
    }

    public void eventReadAesKeyCompleted(Integer result, ByteBuffer attachment) {
        attachment.flip();
        byte[] aesKeyUsingRsaPublicKeyEncrypt = new byte[attachment.remaining()];
        java.util.logging.Logger.getGlobal().info(Arrays.toString(aesKeyUsingRsaPublicKeyEncrypt));
        byte[] aesKey = this.encryptRsa.decrypt(aesKeyUsingRsaPublicKeyEncrypt);
        java.util.logging.Logger.getGlobal().info(Arrays.toString(aesKey));
    }


    public void eventFailed(Throwable exc, ByteBuffer attachment) {

    }
    /* **************************************************************************************
     *  Private Method <Override>
     */

    /* **************************************************************************************
     *  Private Method <Static>
     */
}

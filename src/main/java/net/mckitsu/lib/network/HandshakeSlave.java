package net.mckitsu.lib.network;

import net.mckitsu.lib.network.util.CompletionHandlerEvent;
import net.mckitsu.lib.network.util.EncryptAes;
import net.mckitsu.lib.network.util.EncryptRsa;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Timer;

public class HandshakeSlave extends Handshake {
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */

    /* **************************************************************************************
     *  Variable <Private>
     */
    private final CompletionHandlerEvent<Integer, ByteBuffer> eventReadRsaPublicKey
            = new CompletionHandlerEvent<>(this::eventReadRsaPublicKeyCompleted, this::eventFailed);

    private final CompletionHandlerEvent<Integer, ByteBuffer> eventWriteAesKey
            = new CompletionHandlerEvent<>(this::eventWriteAesKeyCompleted, this::eventFailed);

    private boolean isAction;
    private CompletionHandler<EncryptAes, Void> handler;
    private Network network;
    private EncryptAes encryptAes;
    private EncryptRsa encryptRsa;
    private Timer timer;
    private ByteBuffer byteBufferCache;
    private byte[] cache = {1, 2, 3, 4, 5};

    /* **************************************************************************************
     *  Abstract method <Public>
     */

    /* **************************************************************************************
     *  Abstract method <Protected>
     */

    /* **************************************************************************************
     *  Construct Method
     */
    protected void NetworkHandshakeSlave(){
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
        java.util.logging.Logger.getGlobal().info("action");
        if(this.isAction){
            handler.failed(new IOException("multi action"), null);
            return;
        }

        try {
            this.isAction = true;
            this.handler = handler;
            this.network = network;

            ByteBuffer byteBuffer = ByteBuffer.wrap(this.cache);
            this.network.directWrite(byteBuffer, byteBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    java.util.logging.Logger.getGlobal().info("completed");
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    java.util.logging.Logger.getGlobal().info("failed");
                }
            });

            this.byteBufferCache = ByteBuffer.allocate(16384);
            this.network.directRead(this.byteBufferCache, this.byteBufferCache, this.eventReadRsaPublicKey);
        } catch (Throwable e) {
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
    private void eventReadRsaPublicKeyCompleted(Integer result, ByteBuffer attachment) {
        try {
            attachment.flip();
            byte[] rsaPublicKey = new byte[attachment.remaining()];
            attachment.get(rsaPublicKey, 0, rsaPublicKey.length);
            this.encryptRsa = new EncryptRsa(rsaPublicKey, EncryptRsa.KeyType.PUBLIC_KEY);

            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128,new SecureRandom());
            SecretKey aesKey = keyGen.generateKey();
            this.encryptAes = new EncryptAes(aesKey);

            java.util.logging.Logger.getGlobal().info(Arrays.toString(aesKey.getEncoded()));

            byte[] dataAesKeyAsRsa = this.encryptRsa.encrypt(aesKey.getEncoded());
            if(dataAesKeyAsRsa == null)
                throw new NullPointerException();



            this.byteBufferCache.clear();
            this.byteBufferCache.put(this.cache);
            this.byteBufferCache.flip();
            java.util.logging.Logger.getGlobal().info(this.byteBufferCache.toString());
            this.network.directWrite(this.byteBufferCache, this.byteBufferCache, this.eventWriteAesKey);

        } catch (Throwable e) {
            try {
                this.handler.failed(e, null);
            }catch (Throwable ignore){}
        }
    }

    private void eventWriteAesKeyCompleted(Integer result, ByteBuffer attachment){


    }


    public void eventFailed(Throwable exc, ByteBuffer attachment) {
        try {
            HandshakeSlave.this.handler.failed(exc, null);
        }catch (Throwable ignore){}
    }

    /* **************************************************************************************
     *  Private Method <Override>
     */

    /* **************************************************************************************
     *  Private Method <Static>
     */
}

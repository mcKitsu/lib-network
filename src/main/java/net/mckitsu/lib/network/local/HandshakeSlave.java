package net.mckitsu.lib.network.local;

import net.mckitsu.lib.network.util.CompletionHandlerEvent;
import net.mckitsu.lib.network.util.EncryptAes;
import net.mckitsu.lib.network.util.EncryptRsa;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.channels.CompletionHandler;
import java.security.SecureRandom;

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
    private final CompletionHandlerEvent<byte[], Void> eventReadRsaPublicKey
            = new CompletionHandlerEvent<>(this::eventReadRsaPublicKeyCompleted, this::eventFailed);

    private final CompletionHandlerEvent<byte[], Void> eventWriteAesKey
            = new CompletionHandlerEvent<>(this::eventWriteAesKeyCompleted, this::eventFailed);


    /* **************************************************************************************
     *  Abstract method <Public>
     */

    /* **************************************************************************************
     *  Abstract method <Protected>
     */

    /* **************************************************************************************
     *  Construct Method
     */

    /**
     * construct.
     */
    protected HandshakeSlave(){
        super();
    }

    /* **************************************************************************************
     *  Public Method
     */

    /* **************************************************************************************
     *  Public Method <Override>
     */

    /**
     *
     * @param tcpClientTransferEncrypt network transfer entity.
     * @param attachment user attachment.
     * @param handler compiler handler.
     * @param <A> user attachment type.
     */
    @Override
    public <A> void action(TcpClientTransferEncrypt tcpClientTransferEncrypt, A attachment, CompletionHandler<EncryptAes, A> handler) {
        super.action(tcpClientTransferEncrypt, attachment, handler);

        try {
            this.tcpClientTransferEncrypt.transferDirectRead(null, this.eventReadRsaPublicKey);
        } catch (Throwable exc) {
            super.doFailed(exc);
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

    /**
     * eventReadRsaPublicKeyCompleted
     *
     * @param result read result.
     * @param attachment user attachment.
     */
    private void eventReadRsaPublicKeyCompleted(byte[] result, Void attachment) {

        try {
            this.encryptRsa = new EncryptRsa(result, EncryptRsa.KeyType.PUBLIC_KEY);

            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128,new SecureRandom());
            SecretKey aesKey = keyGen.generateKey();
            this.encryptAes = new EncryptAes(aesKey);

            byte[] dataAesKeyAsRsa = this.encryptRsa.encrypt(aesKey.getEncoded());
            if(dataAesKeyAsRsa == null)
                throw new NullPointerException();

            this.tcpClientTransferEncrypt.transferDirectWrite(dataAesKeyAsRsa, null, this.eventWriteAesKey);

        } catch (Throwable exc) {
            super.doFailed(exc);
        }
    }


    /**
     * eventWriteAesKeyCompleted
     *
     * @param result write result.
     * @param attachment user attachment.
     */
    private void eventWriteAesKeyCompleted(byte[] result, Void attachment){
        super.doCompleted(super.encryptAes);
    }


    /**
     * eventFailed
     *
     * @param exc event exception.
     * @param attachment user attachment.
     */
    public void eventFailed(Throwable exc, Void attachment) {
        super.doFailed(exc);
    }

    /* **************************************************************************************
     *  Private Method <Override>
     */

    /* **************************************************************************************
     *  Private Method <Static>
     */
}

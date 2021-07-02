package net.mckitsu.lib.network.local;

import net.mckitsu.lib.network.util.CompletionHandlerEvent;
import net.mckitsu.lib.network.util.EncryptAes;
import net.mckitsu.lib.network.util.EncryptRsa;

import java.nio.channels.CompletionHandler;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

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
    private final CompletionHandlerEvent<byte[], Void> eventWriteRsaPublicKey
            = new CompletionHandlerEvent<>(this::eventWriteRsaPublicKeyCompleted, this::eventFailed);

    private final CompletionHandlerEvent<byte[], Void> eventReadAesKey
            = new CompletionHandlerEvent<>(this::eventReadAesKeyCompleted, this::eventFailed);

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
    public HandshakeMaster(){
        super();
    }

    /* **************************************************************************************
     *  Public Method
     */

    /* **************************************************************************************
     *  Public Method <Override>
     */

    /**
     * action
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
            KeyPair keyPair = generatorRsaKey();
            this.encryptRsa = new EncryptRsa(keyPair.getPrivate());
            super.tcpClientTransferEncrypt.transferDirectWrite(keyPair.getPublic().getEncoded(), null, this.eventWriteRsaPublicKey);
        } catch (Throwable exc) {
            this.isAction = false;
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
     * generatorRsaKey
     *
     * @return key
     * @throws NoSuchAlgorithmException KeyPairGenerator not found encrypt format.
     */
    private KeyPair generatorRsaKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(512);
        return keyPairGenerator.generateKeyPair();
    }


    /**
     *
     * @param result write result.
     * @param attachment user attachment
     */
    public void eventWriteRsaPublicKeyCompleted(byte[] result, Void attachment) {
        try{
            super.tcpClientTransferEncrypt.transferDirectRead(null, this.eventReadAesKey);
        }catch (Throwable exc){
            super.doFailed(exc);
        }
    }


    /**
     * eventReadAesKeyCompleted
     *
     * @param result read result.
     * @param attachment user attachment.
     */
    public void eventReadAesKeyCompleted(byte[] result, Void attachment) {
        try{
            super.doCompleted(new EncryptAes(this.encryptRsa.decrypt(result)));
        }catch (Throwable exc){
            super.doFailed(exc);
        }
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

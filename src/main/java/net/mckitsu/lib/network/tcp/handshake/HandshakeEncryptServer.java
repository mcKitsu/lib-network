package net.mckitsu.lib.network.tcp.handshake;

import net.mckitsu.lib.network.NetworkException;
import net.mckitsu.lib.network.NetworkExceptionList;
import net.mckitsu.lib.network.tcp.TcpChannel;
import net.mckitsu.lib.util.encrypt.AES;
import net.mckitsu.lib.util.encrypt.RSA;
import net.mckitsu.lib.util.event.CompletionHandlerEvent;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class HandshakeEncryptServer extends HandshakeEncrypt {
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */

    /* **************************************************************************************
     *  Variable <Private>
     */

    protected final CompletionHandlerEvent<Integer, ByteBuffer> completionHandlerEventWriteRsaPublicKey
            = new CompletionHandlerEvent<>(this::handleWriteRsaPublicKey, this::handleWriteFail);

    protected final CompletionHandlerEvent<Integer, ByteBuffer> completionHandlerEventReadAesKey
            = new CompletionHandlerEvent<>(this::handleReadAesKey, this::handleReadFail);
    /* **************************************************************************************
     *  Abstract method <Public>
     */
    public HandshakeEncryptServer(TcpChannel tcpChannel) {
        super(tcpChannel);
    }
    /* **************************************************************************************
     *  Abstract method <Protected>
     */

    /* **************************************************************************************
     *  Construct Method
     */

    /* **************************************************************************************
     *  Public Method
     */

    /* **************************************************************************************
     *  Public Method <Override>
     */


    @Override
    public void accept(CompletionHandlerEvent<AES, HandshakeEncrypt> completionHandlerEvent) {
        if(!super.mutex(completionHandlerEvent, this))
            return;

        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(512);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            this.encryptRSA = new RSA(keyPair.getPrivate());

            ByteBuffer byteBufferRsaPublicKey = ByteBuffer.wrap(keyPair.getPublic().getEncoded());
            this.tcpChannel.write(byteBufferRsaPublicKey, byteBufferRsaPublicKey, this.completionHandlerEventWriteRsaPublicKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            super.executeFail(new NetworkException(NetworkExceptionList.LOCAL_CRASH, e), this);
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
    private void handleWriteRsaPublicKey(Integer result, ByteBuffer attachment){
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        this.tcpChannel.read(byteBuffer, byteBuffer, this.completionHandlerEventReadAesKey);
    }



    private void handleReadAesKey(Integer result, ByteBuffer attachment){
        try{
            attachment.flip();
            byte[] dataAesKeyAsRsa = new byte[attachment.remaining()];
            attachment.get(dataAesKeyAsRsa, 0, dataAesKeyAsRsa.length);

            byte[] aesKey = this.encryptRSA.decrypt(dataAesKeyAsRsa);
            if(aesKey == null)
                throw new NullPointerException();

            this.encryptAES = new AES(aesKey);

            this.encryptRSA = null;
            super.executeCompleted(this.encryptAES, this);

        }catch (Throwable throwable){
            super.executeFail(new NetworkException(NetworkExceptionList.LOCAL_CRASH, throwable), this);
        }
    }

    /* **************************************************************************************
     *  Private Method <Override>
     */

    /* **************************************************************************************
     *  Private Method <Static>
     */
}

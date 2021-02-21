package net.mckitsu.lib.network.tcp.handshake;

import net.mckitsu.lib.network.NetworkException;
import net.mckitsu.lib.network.NetworkExceptionList;
import net.mckitsu.lib.network.tcp.TcpChannel;
import net.mckitsu.lib.util.encrypt.AES;
import net.mckitsu.lib.util.encrypt.RSA;
import net.mckitsu.lib.util.event.CompletionHandlerEvent;

import javax.crypto.SecretKey;
import java.nio.ByteBuffer;

public class HandshakeEncryptClient extends HandshakeEncrypt {
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */

    /* **************************************************************************************
     *  Variable <Private>
     */
    private final CompletionHandlerEvent<Integer, ByteBuffer> completionHandlerEventReadRsaPublicKey
            = new CompletionHandlerEvent<>(this::handleReadRsaPublicKey, super::handleReadFail);

    private final CompletionHandlerEvent<Integer, ByteBuffer> completionHandlerEventWriteAesKeyAsRsa
            = new CompletionHandlerEvent<>(this::handleWriteAesKeyAsRsa, super::handleWriteFail);

    /* **************************************************************************************
     *  Abstract method <Public>
     */

    /* **************************************************************************************
     *  Abstract method <Protected>
     */

    /* **************************************************************************************
     *  Construct Method
     */
    public HandshakeEncryptClient(TcpChannel tcpChannel) {
        super(tcpChannel);
    }


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
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            super.tcpChannel.read(byteBuffer, byteBuffer, this.completionHandlerEventReadRsaPublicKey);
        }catch (Throwable throwable){
            super.executeFail(new NetworkException(NetworkExceptionList.CHANNEL_READ_FAIL, throwable), this);
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
    private void handleReadRsaPublicKey(Integer result, ByteBuffer attachment){
        try {
            attachment.flip();
            byte[] publicKey = new byte[attachment.remaining()];
            attachment.get(publicKey, 0, publicKey.length);

            //public rsa key
            this.encryptRSA = new RSA(publicKey, RSA.KeyType.PUBLIC_KEY);

            //generator aes key

            SecretKey secretKeyAes = super.generateKeyAES();
            super.encryptAES = new AES(secretKeyAes);
            byte[] dataAesKeyAsRsa = this.encryptRSA.encrypt(secretKeyAes.getEncoded());

            if(dataAesKeyAsRsa == null)
                throw new NullPointerException();

            //send aes key using rsa public key encrypt
            ByteBuffer byteBufferAesKeyAsRsa = ByteBuffer.wrap(dataAesKeyAsRsa);
            super.tcpChannel.write(byteBufferAesKeyAsRsa, byteBufferAesKeyAsRsa, completionHandlerEventWriteAesKeyAsRsa);

        } catch (Throwable e) {
            super.executeFail(new NetworkException(NetworkExceptionList.LOCAL_CRASH, e), this);
        }
    }

    private void handleWriteAesKeyAsRsa(Integer result, ByteBuffer attachment){
        this.encryptRSA = null;
        super.executeCompleted(super.encryptAES, this);
    }


    /* **************************************************************************************
     *  Private Method <Override>
     */

    /* **************************************************************************************
     *  Private Method <Static>
     */
}

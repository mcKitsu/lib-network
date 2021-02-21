package net.mckitsu.lib.network.tcp.handshake;

import net.mckitsu.lib.network.NetworkException;
import net.mckitsu.lib.network.NetworkExceptionList;
import net.mckitsu.lib.network.tcp.TcpChannel;
import net.mckitsu.lib.util.encrypt.AES;
import net.mckitsu.lib.util.encrypt.RSA;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public abstract class HandshakeEncrypt extends Handshake<AES, HandshakeEncrypt> {
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */
    protected RSA encryptRSA;
    protected AES encryptAES;
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
    protected HandshakeEncrypt(TcpChannel tcpChannel){
        super(tcpChannel);
    }
    /* **************************************************************************************
     *  Public Method
     */

    /* **************************************************************************************
     *  Public Method <Override>
     */
    @Override
    public AES getResult() {
        return this.encryptAES;
    }

    /* **************************************************************************************
     *  Public Method <Static>
     */

    /* **************************************************************************************
     *  Protected Method
     */
    protected void handleWriteFail(Throwable throwable, ByteBuffer attachment){
        super.executeFail(new NetworkException(NetworkExceptionList.CHANNEL_WRITE_FAIL, throwable), this);
    }

    protected void handleReadFail(Throwable throwable, ByteBuffer attachment){
        super.executeFail(new NetworkException(NetworkExceptionList.CHANNEL_READ_FAIL, throwable), this);
    }

    protected SecretKey generateKeyAES() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128,new SecureRandom());
        return keyGen.generateKey();
    }
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
}

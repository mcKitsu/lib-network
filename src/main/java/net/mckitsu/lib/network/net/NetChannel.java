package net.mckitsu.lib.network.net;

import net.mckitsu.lib.network.tcp.TcpChannel;
import net.mckitsu.lib.network.tcp.TcpClient;
import net.mckitsu.lib.util.EventHandler;
import net.mckitsu.lib.util.encrypt.AES;
import net.mckitsu.lib.util.encrypt.RSA;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.CompletionHandler;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public abstract class NetChannel extends TcpClient {
    private boolean verifyFinish;
    private byte[] aesKey;
    private LifeCycle lifeCycle;
    private AES EncryptAES;
    private RSA EncryptRSA;

    protected abstract void onRead(byte[] data);
    protected abstract void onSend(int identifier);
    protected abstract void onHandshake();
    protected abstract Executor getExecutor();


    public NetChannel(int bufferSize) {
        super(bufferSize);
        this.lifeCycle = LifeCycle.MASTER_NONE;
        this.verifyFinish = false;
    }

    protected NetChannel(TcpChannel tcpChannel) throws IOException {
        super(tcpChannel);

        if(!super.isConnect())
            throw new IOException("TcpChannel is not already connect channel");

        this.lifeCycle = LifeCycle.SLAVE_WAIT_MTU;
        this.verifyFinish = false;
    }

    /* **************************************************************************************
     *  Override method
     */

    @Override
    public boolean send(byte[] data, int identifier) {
        if(this.verifyFinish)
            return this.encryptSend(data, identifier);

        return false;
    }

    @Override
    public boolean connect(InetSocketAddress remoteAddress){
        if(super.isConnect()){
            return false;
        }else {
            this.verifyFinish = false;
            this.lifeCycle = LifeCycle.MASTER_WAIT_RSA_KEY;
            return super.connect(remoteAddress);
        }
    }

    @Override
    public boolean connect(InetSocketAddress remoteAddress, long timeout, TimeUnit unit){
        if(super.isConnect()){
            return false;
        }else{
            this.verifyFinish = false;
            this.lifeCycle = LifeCycle.MASTER_WAIT_RSA_KEY;
            super.connect(remoteAddress, timeout, unit);
            return true;
        }
    }

    @Override
    public <A> boolean connect(InetSocketAddress remoteAddress, A attachment, CompletionHandler<Void, ? super A> handler){
        if(super.isConnect()){
            return false;
        }else{
            this.verifyFinish = false;
            this.lifeCycle = LifeCycle.MASTER_WAIT_RSA_KEY;
            super.connect(remoteAddress, attachment, handler);
            return true;
        }
    }

    @Override
    protected void onTransfer(byte[] data, int identifier){
        if(this.verifyFinish)
            this.onSend(identifier);
    }

    @Override
    protected void onReceiver(byte[] data){
        if(this.verifyFinish){
            byte[] decryptData = this.EncryptAES.decrypt(data);
            if(decryptData != null)
                this.onRead(decryptData);
        }else{
            this.lifeCycle = connectStep(data, this.lifeCycle);
            switch (this.lifeCycle) {
                case SUCCESS:
                    this.verifyFinish = true;
                    this.onHandshake();
                    break;
                case EXCEPTION:
                case ERROR:
                    this.disconnect();
                    break;
            }
        }
    }

    @Override
    protected void onReceiverMtu(int maximumTransmissionUnit) {
        this.lifeCycle = LifeCycle.SLAVE_WAIT_AES_KEY;
        System.out.println("onReceiverMtu size = " + maximumTransmissionUnit);
        if(maximumTransmissionUnit <= 96)
            super.disconnect();

        try {
            this.beginEncrypt();
        } catch (IOException e) {
            super.disconnect();
        }
    }

    /* **************************************************************************************
     *  Private method
     */

    private String byteToString(byte[] data){
        StringBuilder stringBuilder = new StringBuilder();
        for(byte d : data){
            stringBuilder.append(String.format("0x%02X ", d));
        }
        return stringBuilder.toString();
    }

    private boolean encryptSend(byte[] data, int identifier){
        return super.send(EncryptAES.encrypt(data), identifier);
    }

    private LifeCycle connectStep(byte[] data ,LifeCycle lifeCycle){
        System.out.println(lifeCycle);
        switch (lifeCycle) {
            case MASTER_WAIT_RSA_KEY:
                try {
                    EncryptRSA = new RSA(data, RSA.KeyType.PUBLIC_KEY);

                    KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                    keyGen.init(128,new SecureRandom());
                    SecretKey key = keyGen.generateKey();

                    EncryptAES = new AES(key);
                    this.aesKey = key.getEncoded();

                    //write aes key as rsa encrypt
                    super.send(EncryptRSA.encrypt(key.getEncoded()), 0);
                } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException | NullPointerException ignored) {
                    return LifeCycle.EXCEPTION;
                }
                return LifeCycle.SLAVE_WAIT_AES_KEY_RESP;

            case SLAVE_WAIT_AES_KEY:
                try {
                    EncryptAES = new AES(EncryptRSA.decrypt(data));
                    this.encryptSend(EncryptRSA.decrypt(data), 0);

                } catch (NoSuchAlgorithmException | InvalidKeyException | NullPointerException ignored) {
                    return LifeCycle.EXCEPTION;
                }
                return LifeCycle.SUCCESS;

            case SLAVE_WAIT_AES_KEY_RESP:
                if(Arrays.equals(EncryptAES.decrypt(data), this.aesKey)){
                    return LifeCycle.SUCCESS;
                }
                return LifeCycle.ERROR;

            default:
                return LifeCycle.ERROR;
        }
    }

    private void beginEncrypt() throws IOException {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(512);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            this.EncryptRSA = new RSA(keyPair.getPrivate());

            //send rsa key as origin data
            super.send(keyPair.getPublic().getEncoded(),  0);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IOException(e);
        }
    }

    /* **************************************************************************************
     *  Private construct method
     */
    private EventHandler constructEventHandler(){
        return new EventHandler(){
            @Override
            protected Executor getExecutor() {
                return NetChannel.this.getExecutor();
            }
        };
    }
    /* **************************************************************************************
     *  Enum LifeCycle
     */
    protected enum LifeCycle{
        MASTER_NONE,
        MASTER_WAIT_RSA_KEY,
        SLAVE_WAIT_MTU,
        SLAVE_WAIT_AES_KEY,
        SLAVE_WAIT_AES_KEY_RESP,
        SUCCESS,
        EXCEPTION,
        ERROR,
    }

    /* **************************************************************************************
     *  Enum ConnectFailType
     */
}
/* **************************************************************************************
 *  End of file
 */

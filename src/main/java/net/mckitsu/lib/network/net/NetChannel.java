package net.mckitsu.lib.network.net;

import net.mckitsu.lib.network.tcp.TcpChannel;
import net.mckitsu.lib.network.tcp.TcpClient;
import net.mckitsu.lib.util.AES;
import net.mckitsu.lib.util.RSA;

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
    private byte[] verifyKey;
    private LifeCycle lifeCycle;
    private AES EncryptAES;
    private RSA EncryptRSA;

    protected abstract void onRead(byte[] data);
    protected abstract void onSend(int identifier);
    protected abstract void onDisconnect();
    protected abstract void onRemoteDisconnect();
    protected abstract void onConnect();
    protected abstract void onConnectFail(ConnectFailType type);
    protected abstract Executor getExecutor();


    public NetChannel(byte[] verifyKey) throws IOException {
        super();
        this.verifyKey = verifyKey;
        this.lifeCycle = LifeCycle.NONE;
        this.verifyFinish = false;
    }

    protected NetChannel(TcpChannel tcpChannel, byte[] verifyKey) throws IOException {
        super(tcpChannel);

        if(!super.isConnect())
            throw new IOException("TcpChannel is not already connect channel");

        this.verifyKey = verifyKey;
        this.lifeCycle = LifeCycle.WAIT_AES_KEY;
        this.verifyFinish = false;

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
     *  Override method
     */

    @Override
    public synchronized void send(byte[] data, int identifier) {
        if(this.verifyFinish)
            this.encryptSend(data, identifier);
    }

    @Override
    public boolean connect(InetSocketAddress remoteAddress, int identifier){
        if(super.isConnect()){
            doExecutor(() -> this.onConnectFail(ConnectFailType.ALREADY_CONNECT));
            return false;
        }else {
            this.verifyFinish = false;
            this.lifeCycle = LifeCycle.WAIT_RSA_KEY;
            return super.connect(remoteAddress, identifier);
        }
    }

    @Override
    public void connect(InetSocketAddress remoteAddress, long timeout, TimeUnit unit){
        if(super.isConnect()){
            doExecutor(() -> this.onConnectFail(ConnectFailType.ALREADY_CONNECT));
        }else{
            this.verifyFinish = false;
            this.lifeCycle = LifeCycle.WAIT_RSA_KEY;
            super.connect(remoteAddress, timeout, unit);
        }
    }

    @Override
    public void connect(InetSocketAddress remoteAddress, int attachment, CompletionHandler<Void,Integer> handler){
        if(super.isConnect()){
            doExecutor(() -> this.onConnectFail(ConnectFailType.ALREADY_CONNECT));
        }else{
            this.verifyFinish = false;
            this.lifeCycle = LifeCycle.WAIT_RSA_KEY;
            super.connect(remoteAddress, attachment, handler);
        }
    }

    @Override
    protected void onDisconnect(DisconnectType type) {
        if(this.verifyFinish){
            switch (type) {
                case INITIATIVE:
                    doExecutor(this::onDisconnect);
                    break;
                case REMOTE:
                    doExecutor(this::onRemoteDisconnect);
                    break;
            }

        }else{
            if(type == DisconnectType.REMOTE)
                doExecutor(() -> this.onConnectFail(ConnectFailType.VERIFY_FAIL));
        }
    }

    @Override
    protected void onConnect(int identifier){}

    @Override
    protected void onConnectFail(int identifier) {
        this.onConnectFail(ConnectFailType.TIMEOUT);
    }

    @Override
    protected void onTransfer(byte[] data, int identifier){
        if(this.verifyFinish)
            this.doExecutor(() -> this.onSend(identifier));
    }

    @Override
    protected void onReceiver(byte[] data){
        if(this.verifyFinish){
            byte[] decryptData = this.EncryptAES.decrypt(data);
            if(decryptData != null)
                this.onRead(decryptData);
            return;
        }

        this.lifeCycle = connectStep(data, this.lifeCycle);
        switch (this.lifeCycle) {
            case SUCCESS:
                this.verifyFinish = true;
                this.doExecutor(this::onConnect);
                break;
            case EXCEPTION:
                this.disconnect();
                this.doExecutor(() -> this.onConnectFail(ConnectFailType.EXCEPTION));
                break;
            case ERROR:
                this.disconnect();
                this.doExecutor(() -> this.onConnectFail(ConnectFailType.VERIFY_FAIL));
                break;
        }
    }

    /* **************************************************************************************
     *  Private method
     */

    private void encryptSend(byte[] data, int identifier){
        super.send(EncryptAES.encrypt(data), identifier);
    }

    private LifeCycle connectStep(byte[] data ,LifeCycle lifeCycle){
        switch (lifeCycle) {
            case WAIT_RSA_KEY:
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
                return LifeCycle.WAIT_AES_KEY_RESP;

            case WAIT_AES_KEY:
                try {
                    EncryptAES = new AES(EncryptRSA.decrypt(data));
                    this.encryptSend(EncryptRSA.decrypt(data), 0);

                } catch (NoSuchAlgorithmException | InvalidKeyException | NullPointerException ignored) {
                    return LifeCycle.EXCEPTION;
                }
                return LifeCycle.WAIT_VERIFY_KEY;

            case WAIT_AES_KEY_RESP:
                if(Arrays.equals(EncryptAES.decrypt(data), this.aesKey)){
                    this.encryptSend(this.verifyKey, 0);
                    return LifeCycle.WAIT_SUCCESS;
                }
                return LifeCycle.ERROR;

            case WAIT_VERIFY_KEY:
                if(Arrays.equals(EncryptAES.decrypt(data), this.verifyKey)){
                    this.encryptSend(this.verifyKey, 0);
                    return LifeCycle.SUCCESS;
                }
                return LifeCycle.ERROR;
            case WAIT_SUCCESS:
                if(Arrays.equals(EncryptAES.decrypt(data), this.verifyKey)) {
                    return LifeCycle.SUCCESS;
                }
            default:
                return LifeCycle.ERROR;
        }
    }

    /* **************************************************************************************
     *  Protected Class/Enum
     */

    protected enum LifeCycle{
        NONE,
        WAIT_RSA_KEY,
        WAIT_AES_KEY,
        WAIT_AES_KEY_RESP,
        WAIT_VERIFY_KEY,
        WAIT_SUCCESS,
        SUCCESS,
        EXCEPTION,
        ERROR,
    }

    public enum ConnectFailType{
        TIMEOUT,
        EXCEPTION,
        VERIFY_FAIL,
        ALREADY_CONNECT,
    }
}
/* **************************************************************************************
 *  End of file
 */

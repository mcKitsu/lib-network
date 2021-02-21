package net.mckitsu.lib.network.tcp;

import net.mckitsu.lib.network.Client;
import net.mckitsu.lib.network.ClientEvent;
import net.mckitsu.lib.network.tcp.handshake.*;
import net.mckitsu.lib.util.encrypt.AES;
import net.mckitsu.lib.util.event.CompletionHandlerEvent;

import java.net.SocketAddress;

/**
 * TCP Asynchronous Client.
 *
 * @author  ZxyKira
 */

public class TcpClient implements Client {
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */
    protected final TcpChannel tcpChannel;

    /* **************************************************************************************
     *  Variable <Private>
     */
    private boolean alreadyOnRemoteDisconnect = false;
    private final HandshakeMtu networkHandshakeMtu;
    private final HandshakeEncrypt networkHandshakeEncrypt;
    private final ClientEvent tcpClientEvent;

    private final CompletionHandlerEvent<Integer, HandshakeMtu> completionHandlerEventNetworkHandshakeMtu
            = new CompletionHandlerEvent<>(this::handleNetworkHandshakeMtu, this::handleNetworkHandshakeMtuFail);

    private final CompletionHandlerEvent<AES, HandshakeEncrypt> completionHandlerEventNetworkHandshakeEncrypt
            = new CompletionHandlerEvent<>(this::handleNetworkHandshakeEncrypt, this::handleNetworkHandshakeEncryptFail);

    private final CompletionHandlerEvent<Void, Void> completionHandlerEventConnect
            = new CompletionHandlerEvent<>(this::handleConnect, this::handleConnectFail);

    /* **************************************************************************************
     *  Abstract method <Public>
     */

    /* **************************************************************************************
     *  Abstract method <Protected>
     */

    /* **************************************************************************************
     *  Construct Method
     */
    public TcpClient(int maximumTransmissionUnit, ClientEvent tcpClientEvent){
        this.tcpChannel = new TcpChannel();
        this.tcpClientEvent = tcpClientEvent;
        this.networkHandshakeMtu = new HandshakeMtuClient(this.tcpChannel, maximumTransmissionUnit);
        this.networkHandshakeEncrypt = new HandshakeEncryptClient(this.tcpChannel);
    }

    public TcpClient(TcpChannel tcpChannel, int maximumTransmissionUnit, ClientEvent tcpClientEvent){
        this.tcpChannel = tcpChannel;
        this.tcpClientEvent = tcpClientEvent;
        this.networkHandshakeMtu = new HandshakeMtuServer(this.tcpChannel, maximumTransmissionUnit);
        this.networkHandshakeEncrypt = new HandshakeEncryptServer(this.tcpChannel);
        this.networkHandshakeMtu.accept(this.completionHandlerEventNetworkHandshakeMtu);
    }

    /* **************************************************************************************
     *  Public Method
     */

    /* **************************************************************************************
     *  Public Method <Override>
     */
    @Override
    public void connect(SocketAddress socketAddress){
        this.tcpChannel.connect(socketAddress, null, this.completionHandlerEventConnect);
    }

    @Override
    public synchronized void close(){
        if(this.isConnect()){
            this.close();
            this.onDisconnect();
        }
    }

    @Override
    public SocketAddress getLocalAddress() {
        return this.tcpChannel.getLocalAddress();
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return this.tcpChannel.getRemoteAddress();
    }

    @Override
    public boolean isConnect() {
        return this.tcpChannel.isConnect();
    }

    @Override
    public boolean isOpen() {
        return this.tcpChannel.isOpen();
    }

    @Override
    public void write(byte[] data) {

    }

    /* **************************************************************************************
     *  Public Method <Static>
     */

    /* **************************************************************************************
     *  Protected Method
     */
    protected void onConnect(){
        this.alreadyOnRemoteDisconnect = false;
        try{
            this.tcpClientEvent.onConnect(this);
        }catch (Throwable ignore){}
    }

    protected void onConnectFail(){
        try{
            this.tcpClientEvent.onConnectFail(this);
        }catch (Throwable ignore){}
    }

    protected void onDisconnect(){
        try{
            this.tcpClientEvent.onDisconnect(this);
        }catch (Throwable ignore){}
    }

    protected void onDisconnectRemote(){
        synchronized (this){
            if(this.alreadyOnRemoteDisconnect)
                return;
        }

        this.alreadyOnRemoteDisconnect = true;
        this.tcpChannel.close();
        try {
            this.tcpClientEvent.onDisconnectRemote(this);
        }catch (Throwable ignore){}
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

    private void handleNetworkHandshakeMtu(Integer result, HandshakeMtu attachment){
        this.networkHandshakeEncrypt.accept(this.completionHandlerEventNetworkHandshakeEncrypt);
        this.onConnect();
    }

    private void handleNetworkHandshakeMtuFail(Throwable throwable, HandshakeMtu attachment){
        this.tcpChannel.close();
        this.onConnectFail();
    }

    private void handleNetworkHandshakeEncrypt(AES result, HandshakeEncrypt attachment){
        System.out.println("handleNetworkHandshakeEncrypt");
    }

    private void handleNetworkHandshakeEncryptFail(Throwable throwable, HandshakeEncrypt attachment){
        System.out.println("handleNetworkHandshakeEncryptFail");
    }

    private void handleConnect(Void result, Void attachment){
        this.networkHandshakeMtu.accept(this.completionHandlerEventNetworkHandshakeMtu);
    }

    private void handleConnectFail(Throwable throwable, Void attachment){
        this.onConnectFail();
    }
    /* **************************************************************************************
     *  Private Method <Override>
     */

    /* **************************************************************************************
     *  Private Method <Static>
     */
}

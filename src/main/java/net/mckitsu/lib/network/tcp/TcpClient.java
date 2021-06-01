package net.mckitsu.lib.network.tcp;

import net.mckitsu.lib.network.Client;
import net.mckitsu.lib.network.ClientEvent;
import net.mckitsu.lib.network.tcp.handler.HandlerTransceiver;
import net.mckitsu.lib.network.tcp.handshake.*;
import net.mckitsu.lib.util.encrypt.AES;
import net.mckitsu.lib.util.event.CompletionHandlerEvent;

import java.net.SocketAddress;

/**
 * TCP Asynchronous Client.
 *
 * @author  ZxyKira
 */

public class TcpClient implements Client{
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
    private final HandshakeMtu handshakeMtu;
    private final HandshakeEncrypt handshakeEncrypt;
    private final HandlerTransceiver handlerTransceiver;
    private final HandlerTransceiver.EventEntity handlerTransceiverEvent;

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
        this.handshakeMtu = new HandshakeMtuClient(this.tcpChannel, maximumTransmissionUnit);
        this.handshakeEncrypt = new HandshakeEncryptClient(this.tcpChannel);
        this.handlerTransceiver = new HandlerTransceiver();
        this.handlerTransceiverEvent = new HandlerTransceiver.EventEntity();
        this.handlerTransceiverEvent.onTransfer = this::onTransfer;
        this.handlerTransceiverEvent.onReceiver = this::onReceiver;
        this.handlerTransceiverEvent.onTransceiverFail = this::onTransceiverFail;
    }

    public TcpClient(TcpChannel tcpChannel, int maximumTransmissionUnit, ClientEvent tcpClientEvent){
        this.tcpChannel = tcpChannel;
        this.tcpClientEvent = tcpClientEvent;
        this.handshakeMtu = new HandshakeMtuServer(this.tcpChannel, maximumTransmissionUnit);
        this.handshakeEncrypt = new HandshakeEncryptServer(this.tcpChannel);
        this.handshakeMtu.accept(this.completionHandlerEventNetworkHandshakeMtu);
        this.handlerTransceiver = new HandlerTransceiver();
        this.handlerTransceiverEvent = new HandlerTransceiver.EventEntity();
        this.handlerTransceiverEvent.onTransfer = this::onTransfer;
        this.handlerTransceiverEvent.onReceiver = this::onReceiver;
        this.handlerTransceiverEvent.onTransceiverFail = this::onTransceiverFail;
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
        try{
            this.handlerTransceiver.write(data);
        }catch (Throwable ignore){}
    }

    /* **************************************************************************************
     *  Public Method <Static>
     */

    /* **************************************************************************************
     *  Protected Method
     */
    protected void onConnect(){
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
        try {
            this.tcpClientEvent.onDisconnectRemote(this);
        }catch (Throwable ignore){}
    }

    protected void onTransfer(byte[] data){
        try {
            this.tcpClientEvent.onTransfer(this, data);
        }catch (Throwable ignore){}
    }

    protected void onReceiver(byte[] data){
        try {
            this.tcpClientEvent.onReceiver(this, data);
        }catch (Throwable ignore){}
    }

    protected void onTransceiverFail(){
        this.tcpChannel.close();
        try{
            this.onDisconnectRemote();
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
        System.out.println(attachment.getResult());
        this.handshakeEncrypt.accept(this.completionHandlerEventNetworkHandshakeEncrypt);
    }

    private void handleNetworkHandshakeMtuFail(Throwable throwable, HandshakeMtu attachment){
        this.tcpChannel.close();
        this.onConnectFail();
    }

    private void handleNetworkHandshakeEncrypt(AES result, HandshakeEncrypt attachment){
        this.handlerTransceiver.start(this.tcpChannel,
                this.handshakeEncrypt.getResult(),
                this.handshakeMtu.getResult(),
                this.handlerTransceiverEvent);
        this.onConnect();
    }

    private void handleNetworkHandshakeEncryptFail(Throwable throwable, HandshakeEncrypt attachment){
        this.tcpChannel.close();
        this.onConnectFail();
    }

    private void handleConnect(Void result, Void attachment){
        this.handshakeMtu.accept(this.completionHandlerEventNetworkHandshakeMtu);
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

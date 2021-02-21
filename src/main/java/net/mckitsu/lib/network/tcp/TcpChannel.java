package net.mckitsu.lib.network.tcp;

import net.mckitsu.lib.network.Channel;
import net.mckitsu.lib.network.NetworkException;
import net.mckitsu.lib.network.NetworkExceptionList;
import net.mckitsu.lib.util.event.CompletionHandlerEvent;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

public class TcpChannel implements Channel {
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */
    protected AsynchronousSocketChannel channel;
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
    public TcpChannel(){
        this.channel = null;
    }

    protected TcpChannel(AsynchronousSocketChannel asynchronousSocketChannel){
        this.channel = asynchronousSocketChannel;
    }

    /* **************************************************************************************
     *  Public Method
     */

    /* **************************************************************************************
     *  Public Method <Override>
     */
    @Override
    public <A> void connect(SocketAddress remoteAddress, A attachment, CompletionHandlerEvent<Void, A> handlerEvent) {
        if(isConnect()) {
            handlerEvent.failed(new NetworkException(NetworkExceptionList.ALREADY_CONNECT), attachment);
            return;
        }

        if(this.channel==null){
            try {
                this.channel = AsynchronousSocketChannel.open();
            } catch (IOException e) {
                handlerEvent.failed(new NetworkException(NetworkExceptionList.CHANNEL_OPEN_FAIL, e), attachment);
                return;
            }
        }

        if(!this.channel.isOpen()){
            try {
                this.channel = AsynchronousSocketChannel.open();
            } catch (IOException e) {
                handlerEvent.failed(new NetworkException(NetworkExceptionList.CHANNEL_OPEN_FAIL, e), attachment);
                return;
            }
        }

        this.channel.connect(remoteAddress, attachment, handlerEvent);
    }

    @Override
    public boolean isConnect() {
        return this.getRemoteAddress() != null;
    }

    @Override
    public boolean isOpen() {
        try {
            return this.channel.isOpen();
        }catch (NullPointerException e){
            return false;
        }
    }

    @Override
    public void close() {
        try {
            this.channel.close();
        } catch (IOException|NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SocketAddress getLocalAddress() {
        try{
            return this.channel.getLocalAddress();
        } catch (Throwable e) {
            return null;
        }
    }

    @Override
    public SocketAddress getRemoteAddress() {
        try{
            return this.channel.getRemoteAddress();
        } catch (Throwable e) {
            return null;
        }
    }

    @Override
    public <A> void read(ByteBuffer byteBuffer, A attachment, CompletionHandlerEvent<Integer, A> handlerEvent) {
        try{
            channel.read(byteBuffer, attachment, handlerEvent);
        }catch (Throwable e){
            handlerEvent.failed(new NetworkException(NetworkExceptionList.CHANNEL_READ_FAIL, e), attachment);
        }
    }

    @Override
    public <A> void write(ByteBuffer byteBuffer, A attachment, CompletionHandlerEvent<Integer, A> handlerEvent) {
        try {
            channel.write(byteBuffer, attachment, handlerEvent);
        }catch (Throwable e){
            handlerEvent.failed(new NetworkException(NetworkExceptionList.CHANNEL_WRITE_FAIL, e), attachment);
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

    /* **************************************************************************************
     *  Private Method <Override>
     */

    /* **************************************************************************************
     *  Private Method <Static>
     */

}

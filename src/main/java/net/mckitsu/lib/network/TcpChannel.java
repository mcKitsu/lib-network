package net.mckitsu.lib.network;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class TcpChannel {
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

    public TcpChannel(AsynchronousSocketChannel asynchronousSocketChannel){
        this.channel = asynchronousSocketChannel;
    }

    /* **************************************************************************************
     *  Public Method
     */

    /* **************************************************************************************
     *  Public Method <Override>
     */

    /*----------------------------------------
     *  connect
     *----------------------------------------*/
    public <A> void connect(SocketAddress remoteAddress, A attachment, CompletionHandler<Void,? super A> handler) {
        if(isConnect()) {
            handler.failed(null , attachment);
            return;
        }

        if(this.channel==null){
            try {
                this.channel = AsynchronousSocketChannel.open();
            } catch (IOException e) {
                try{
                    handler.failed(e, attachment);
                }catch (Throwable ignore){}
                return;
            }
        }

        if(!this.channel.isOpen()){
            try {
                this.channel = AsynchronousSocketChannel.open();
            } catch (IOException e) {
                try{
                    handler.failed(e, attachment);
                }catch (Throwable ignore){}

                return;
            }
        }

        this.channel.connect(remoteAddress, attachment, handler);
    }



    /*----------------------------------------
     *  isConnect
     *----------------------------------------*/
    public boolean isConnect() {
        return this.getRemoteAddress() != null;
    }



    /*----------------------------------------
     *  isOpen
     *----------------------------------------*/
    public boolean isOpen() {
        try {
            return this.channel.isOpen();

        }catch (NullPointerException e){
            return false;
        }
    }



    /*----------------------------------------
     *  close
     *----------------------------------------*/
    public void close() {
        try {
            this.channel.close();
        } catch (Throwable ignore) {}
    }



    /*----------------------------------------
     *  getLocalAddress
     *----------------------------------------*/
    public SocketAddress getLocalAddress() {
        try{
            return this.channel.getLocalAddress();
        } catch (Throwable e) {
            return null;
        }
    }



    /*----------------------------------------
     *  getRemoteAddress
     *----------------------------------------*/
    public SocketAddress getRemoteAddress() {
        try{
            return this.channel.getRemoteAddress();
        } catch (Throwable e) {
            return null;
        }
    }



    /*----------------------------------------
     *  read
     *----------------------------------------*/
    public <A> void read(ByteBuffer byteBuffer, A attachment, CompletionHandler<Integer, A> handlerEvent) {
        try{
            channel.read(byteBuffer, attachment, handlerEvent);
        }catch (Throwable e){
            handlerEvent.failed(e, attachment);
        }
    }



    /*----------------------------------------
     *  write
     *----------------------------------------*/
    public <A> void write(ByteBuffer byteBuffer, A attachment, CompletionHandler<Integer, A> handlerEvent) {
        try {
            channel.write(byteBuffer, attachment, handlerEvent);
        }catch (Throwable e){
            handlerEvent.failed(e, attachment);
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

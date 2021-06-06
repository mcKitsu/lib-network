package net.mckitsu.lib.network.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class TcpListener{
    /* **************************************************************************************
     *  Variable <Public>
     */




    /* **************************************************************************************
     *  Variable <Protected>
     */
    protected AsynchronousServerSocketChannel serverChannel;



    /* **************************************************************************************
     *  Variable <Private>
     */
    private CompletionHandler<TcpChannel, Void> handlerAccept;
    private final CompletionHandler<AsynchronousSocketChannel, Object> channelAccept;



    /* **************************************************************************************
     *  Abstract method <Public>
     */

    /* **************************************************************************************
     *  Abstract method <Protected>
     */

    /* **************************************************************************************
     *  Construct method
     */
    public TcpListener() {
        this.channelAccept = this.constructChannelAccept();
    }



    /* **************************************************************************************
     *  Public Method
     */

    /*----------------------------------------
     *  isStart
     *----------------------------------------*/
    public boolean isStart(){
        try {
            return this.serverChannel.getLocalAddress() != null;
        } catch (IOException|NullPointerException e) {
            return false;
        }
    }



    /*----------------------------------------
     *  start
     *----------------------------------------*/
    public void start(InetSocketAddress hostAddress, CompletionHandler<TcpChannel, Void> handler){
        if(!isStart()){
            this.handlerAccept = handler;
            try {
                this.serverChannel = AsynchronousServerSocketChannel.open();
                this.serverChannel.bind(hostAddress);
                this.serverChannel.accept(null, this.channelAccept);
            } catch (IOException e) {
                try{
                    handler.failed(e, null);
                }catch (Throwable ignore){}
            }
        }
    }



    /*----------------------------------------
     *  stop
     *----------------------------------------*/
    public void stop(){
        try {
            this.serverChannel.close();
        } catch (IOException|NullPointerException ignore){}
    }



    /*----------------------------------------
     *  getLocalAddress
     *----------------------------------------*/
    public SocketAddress getLocalAddress() {
        try {
            return this.serverChannel.getLocalAddress();
        } catch (IOException|NullPointerException e) {
            return null ;
        }
    }



    /* **************************************************************************************
     *  Public Method <Override>
     */

    /* **************************************************************************************
     *  Public Method <Static>
     */

    /* **************************************************************************************
     *  protected method
     */

    /* **************************************************************************************
     *  private method
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
    /*----------------------------------------
     *  constructChannelAccept
     *----------------------------------------*/
    private CompletionHandler<AsynchronousSocketChannel, Object> constructChannelAccept(){
        return new CompletionHandler<AsynchronousSocketChannel, Object>() {
            @Override
            public void completed(AsynchronousSocketChannel result, Object attachment) {
                TcpListener.this.serverChannel.accept(null, this);
                TcpListener.this.handlerAccept.completed(new TcpChannel(result), null);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {

            }
        };
    }



    /* **************************************************************************************
     *  Private Method <Override>
     */

    /* **************************************************************************************
     *  Private Method <Static>
     */
}

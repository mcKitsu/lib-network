package net.mckitsu.lib.network.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class TcpListener implements CompletionHandler<AsynchronousSocketChannel, Object>{
    /* **************************************************************************************
     *  Variable <Public>
     */
    private CompletionHandler<TcpChannel, Void> handlerAccept;



    /* **************************************************************************************
     *  Variable <Protected>
     */
    protected AsynchronousServerSocketChannel serverChannel;



    /* **************************************************************************************
     *  Variable <Private>
     */

    /* **************************************************************************************
     *  Abstract method <Public>
     */
    /*----------------------------------------
     *  completed
     *----------------------------------------*/
    @Override
    public void completed(AsynchronousSocketChannel result, Object attachment) {
        this.serverChannel.accept(null, this);
        this.handlerAccept.completed(new TcpChannel(result), null);
    }



    /*----------------------------------------
     *  failed
     *----------------------------------------*/
    @Override
    public void failed(Throwable exc, Object attachment) {
        this.handlerAccept.failed(exc, null);
    }

    /* **************************************************************************************
     *  Abstract method <Protected>
     */

    /* **************************************************************************************
     *  Construct method
     */
    public TcpListener() {
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
            try {
                this.handlerAccept = handler;
                this.serverChannel = AsynchronousServerSocketChannel.open();
                this.serverChannel.bind(hostAddress);
                this.serverChannel.accept(null, this);
            } catch (IOException e) {
                handler.failed(e, null);
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

    /* **************************************************************************************
     *  Private Method <Override>
     */

    /* **************************************************************************************
     *  Private Method <Static>
     */
}

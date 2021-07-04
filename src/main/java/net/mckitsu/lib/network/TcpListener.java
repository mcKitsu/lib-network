package net.mckitsu.lib.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.*;
import java.util.function.Consumer;

public class TcpListener implements CompletionHandler<AsynchronousSocketChannel, Void>{
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */
    protected AsynchronousServerSocketChannel asynchronousServerSocketChannel;


    /* **************************************************************************************
     *  Variable <Private>
     */ 

    /* **************************************************************************************
     *  Abstract method <Public>
     */
    private Consumer onAccept;

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
    public boolean isOpen(){
        try {
            return this.asynchronousServerSocketChannel.getLocalAddress() != null;
        } catch (IOException|NullPointerException e) {
            return false;
        }
    }


    /*----------------------------------------
     *  start
     *----------------------------------------*/
    public boolean start(InetSocketAddress hostAddress, Consumer<TcpChannel> completion){
        if(this.isOpen())
            return false;

        try {
            this.asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open();
            this.asynchronousServerSocketChannel.bind(hostAddress);
            this.onAccept = completion;
            this.accept();
        } catch (IOException e) {
            return false;
        }
        return true;
    }


    /*----------------------------------------
     *  stop
     *----------------------------------------*/
    public boolean stop(){
        try {
            this.asynchronousServerSocketChannel.close();
            this.asynchronousServerSocketChannel = null;
        } catch (IOException|NullPointerException ignore){
            return false;
        }
        return true;
    }


    /*----------------------------------------
     *  getLocalAddress
     *----------------------------------------*/
    public SocketAddress getLocalAddress() {
        try {
            return this.asynchronousServerSocketChannel.getLocalAddress();
        } catch (IOException|NullPointerException e) {
            return null ;
        }
    }


    /* **************************************************************************************
     *  Public Method <Override>
     */
    /*----------------------------------------
     *  completed
     *----------------------------------------*/
    @Override
    public void completed(AsynchronousSocketChannel result, Void attachment) {
        this.accept();
        this.onAccept.accept(new TcpChannel(result));
    }


    /*----------------------------------------
     *  failed
     *----------------------------------------*/
    @Override
    public void failed(Throwable exc, Void attachment) {
        this.stop();
    }


    /* **************************************************************************************
     *  Public Method <Static>
     */

    /* **************************************************************************************
     *  protected method
     */

    /* **************************************************************************************
     *  private method
     */
    private void accept(){
        try {
            this.asynchronousServerSocketChannel.accept(null, this);
        }catch (NotYetBoundException| ShutdownChannelGroupException|NullPointerException e){
            java.util.logging.Logger.getGlobal().warning(e.toString());
            this.stop();
        }
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

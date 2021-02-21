package net.mckitsu.lib.network.tcp;

import net.mckitsu.lib.util.event.CompletionHandlerEvent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * TCP Asynchronous Server.
 *
 * @author  ZxyKira
 */
public abstract class TcpListener {
    protected AsynchronousServerSocketChannel serverChannel;
    private final CompletionHandlerEvent<AsynchronousSocketChannel, Object> chEventAccept;

    /* **************************************************************************************
     *  Abstract method
     */

    /**
     * 服務端啟動失敗後調用此方法
     *
     */
    public abstract void onOpenFail(Throwable e);

    /**
     * 服務端接受客戶端成功後調用此方法，建議依下列方法使用客戶端.
     *
     * @param tcpChannel 已連線的TcpChannel
     */
    public abstract void onAccept(TcpChannel tcpChannel);

    /* **************************************************************************************
     *  Construct method
     */

    public TcpListener() {
        this.chEventAccept = new CompletionHandlerEvent<>(this::handleAccept, this::handleAcceptFail);
    }

    /* **************************************************************************************
     *  Override method
     */

    /* **************************************************************************************
     *  Public method
     */

    public boolean isStart(){
        try {
            return this.serverChannel.getLocalAddress() != null;
        } catch (IOException|NullPointerException e) {
            return false;
        }
    }

    /**
     * 開始監聽Socket TCP
     *
     */
    public void start(InetSocketAddress hostAddress){
        if(!isStart()){
            try {
                this.serverChannel = AsynchronousServerSocketChannel.open();
                this.serverChannel.bind(hostAddress);
                this.serverChannel.accept(null, chEventAccept);
            } catch (IOException e) {
                this.onOpenFail(e);
            }
        }
    }

    /**
     * 停止監聽Socket TCP
     *
     */
    public void stop(){
        try {
            this.serverChannel.close();
        } catch (IOException|NullPointerException ignore){}
    }

    /**
     * 取得本地監聽的SocketAddress
     *
     * @return SocketAddress
     */
    public SocketAddress getLocalAddress() {
        try {
            return this.serverChannel.getLocalAddress();
        } catch (IOException|NullPointerException e) {
            return null ;
        }
    }

    /* **************************************************************************************
     *  protected method
     */

    protected void accept(){
        this.serverChannel.accept(null, this.chEventAccept);
    }

    /* **************************************************************************************
     *  private method
     */
    private void handleAccept(AsynchronousSocketChannel result, Object attachment){
        if(this.isStart()){
            this.accept();
            this.onAccept(new TcpChannel(result));
        }
    }

    private void handleAcceptFail(Throwable exc, Object attachment){
    }
}


package net.mckitsu.lib.network.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * TCP Asynchronous Server.
 *
 * @author  ZxyKira
 */
public abstract class TcpListener {
    protected AsynchronousServerSocketChannel serverChannel;
    private final CompletionHandlerEvent completionHandlerEvent;

    /* **************************************************************************************
     *  Abstract method
     */

    /**
     * 服務端啟動失敗後調用此方法
     *
     * @param e IOException.
     */
    public abstract void onOpenFail(IOException e);

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
        this.completionHandlerEvent = new CompletionHandlerEvent() {

            @Override
            protected boolean isStart() {
                return TcpListener.this.isStart();
            }

            @Override
            protected void accept() {
                TcpListener.this.accept();
            }

            @Override
            protected void onAccept(TcpChannel tcpChannel) {
                TcpListener.this.onAccept(tcpChannel);
            }
        };
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
                this.serverChannel.accept(null, completionHandlerEvent);
            } catch (IOException e) {
                onOpenFail(e);
            }
        }
    }

    /**
     * 停止監聽Socket TCP
     *
     */
    public void stop(){
        try {
            serverChannel.close();
        } catch (IOException|NullPointerException ignore){}
    }

    /**
     * 取得本地監聽的SocketAddress
     *
     * @return SocketAddress
     * @throws IOException Server並未開始監聽.
     */
    public SocketAddress getLocalAddress() throws IOException {
        try {
            return serverChannel.getLocalAddress();
        } catch (IOException|NullPointerException e) {
            throw new IOException("Server not open.");
        }
    }

    /* **************************************************************************************
     *  protected method
     */

    protected void accept(){
        System.out.println("TcpListener::accept");
        serverChannel.accept(null, this.completionHandlerEvent);
    }

    /* **************************************************************************************
     *  Class CompletionHandlerEvent
     */

    private static abstract class CompletionHandlerEvent implements CompletionHandler<AsynchronousSocketChannel, Object>{
        protected abstract boolean isStart();
        protected abstract void accept();
        protected abstract void onAccept(TcpChannel tcpChannel);

        @Override
        public void completed(AsynchronousSocketChannel channel, Object object) {
            if(this.isStart()){
                this.accept();
                this.onAccept(new TcpChannel(channel));
            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {}
    }
}


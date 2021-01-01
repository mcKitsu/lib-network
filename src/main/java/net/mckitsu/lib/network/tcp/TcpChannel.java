package net.mckitsu.lib.network.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TcpChannel {
    protected AsynchronousSocketChannel channel;

    /* **************************************************************************************
     *  Abstract method
     */

    /* **************************************************************************************
     *  Construct method
     */

    public TcpChannel(){
        this.channel = null;
    }

    public TcpChannel(AsynchronousSocketChannel channel){
        this.channel = channel;
    }

    /* **************************************************************************************
     *  Static method
     */

    /* **************************************************************************************
     *  Override method
     */

    /* **************************************************************************************
     *  Public method
     */

    public boolean connect(InetSocketAddress remoteAddress, long timeout, TimeUnit unit) throws IOException{
        if(this.channel == null)
            this.channel = AsynchronousSocketChannel.open();

        if(this.isConnect())
            return false;

        if(!this.channel.isOpen())
            this.channel = AsynchronousSocketChannel.open();

        Future<Void> connectResult = channel.connect(remoteAddress);

        try {
            connectResult.get(timeout, unit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new IOException("Connect Fail " + remoteAddress);
        }
        return true;
    }

    public <A> boolean connect(InetSocketAddress remoteAddress, A attachment, CompletionHandler<Void, ? super A> handler) throws IOException {
        if(this.channel == null)
            this.channel = AsynchronousSocketChannel.open();

        if(this.isConnect())
            return false;

        if(!this.channel.isOpen())
            this.channel = AsynchronousSocketChannel.open();

        this.channel.connect(remoteAddress, attachment, handler);
        return true;
    }

    /**
     * 停止與遠端連線.
     *
     */
    public boolean disconnect(){
        if(this.channel == null)
            return false;

        if(this.channel.isOpen()) {
            try {
                this.channel.close();
                return true;
            } catch (IOException ignore) {
                return false;
            }
        }
        return false;
    }

    /**
     * 是否已連線
     *
     */
    public boolean isConnect(){
        try {
            return channel.getRemoteAddress() != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 取得本地SocketAddress.
     *
     * @return 本地的SocketAddress,Null表示尚未連線.
     */
    public SocketAddress getLocalAddress() {
        try {
            return channel.getLocalAddress();
        } catch (IOException | NullPointerException e) {
            return null;
        }
    }

    /**
     * 取得遠端SocketAddress.
     *
     * @return 目標的SocketAddress,Null表示尚未連線.
     */
    public SocketAddress getRemoteAddress() {
        try {
            return channel.getRemoteAddress();
        } catch (IOException | NullPointerException e) {
            return null;
        }
    }

    /* **************************************************************************************
     *  protected method
     */

    /* **************************************************************************************
     *  Private method
     */
}

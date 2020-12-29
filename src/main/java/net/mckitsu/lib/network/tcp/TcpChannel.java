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

    protected TcpChannel(AsynchronousSocketChannel channel){
        if(channel == null)
            throw new NullPointerException();

        this.channel = channel;
    }

    /* **************************************************************************************
     *  Static method
     */

    public static TcpChannel open() throws IOException {
        return new TcpChannel(AsynchronousSocketChannel.open());
    }
    /* **************************************************************************************
     *  Override method
     */

    /* **************************************************************************************
     *  Public method
     */

    public void connect(InetSocketAddress remoteAddress, long timeout, TimeUnit unit) throws IOException{
        if(this.isConnect())
            throw new IOException("Already connect to remote.");

        if(!this.channel.isOpen())
            this.channel = AsynchronousSocketChannel.open();

        Future<Void> connectResult = channel.connect(remoteAddress);

        try {
            connectResult.get(timeout, unit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new IOException("Connect Fail " + remoteAddress);
        }
    }

    public void connect(InetSocketAddress remoteAddress, int attachment, CompletionHandler<Void,Integer> handler) throws IOException {
        if(this.isConnect())
            throw new IOException("Already connect to remote.");

        if(!this.channel.isOpen())
            this.channel = AsynchronousSocketChannel.open();

        channel.connect(remoteAddress, attachment, handler);
    }

    /**
     * 停止與遠端連線.
     *
     */
    public boolean disconnect(){
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

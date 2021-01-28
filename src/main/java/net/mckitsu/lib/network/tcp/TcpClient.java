package net.mckitsu.lib.network.tcp;

import net.mckitsu.lib.util.pool.ByteBufferPool;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * TCP Asynchronous Client.
 *
 * @author  ZxyKira
 */

public abstract class TcpClient extends TcpChannel{
    private final Queue<byte[]> sendQueue;
    private ByteBufferPool byteBufferPool;

    /* **************************************************************************************
     *  Abstract method
     */
    protected abstract void onSend(byte[] data);

    protected abstract void onRead(byte[] data);

    /* **************************************************************************************
     *  Construct method
     */
    public TcpClient(){
        this.sendQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * 建構子.
     *
     * @param tcpChannel AsynchronousSocketChannel
     */
    public TcpClient(TcpChannel tcpChannel){
        super(tcpChannel);
        this.sendQueue = new ConcurrentLinkedQueue<>();
    }

    /* **************************************************************************************
     *  Override method
     */


    /* **************************************************************************************
     *  Public method
     */

    /* **************************************************************************************
     *  protected method
     */

    /* **************************************************************************************
     *  Private method
     */


    /* **************************************************************************************
     *  Class DataPacket
     */
}

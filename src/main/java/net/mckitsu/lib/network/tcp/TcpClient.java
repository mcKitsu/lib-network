package net.mckitsu.lib.network.tcp;

import net.mckitsu.lib.util.pool.ByteBufferPool;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * TCP Asynchronous Client.
 *
 * @author  ZxyKira
 */

public abstract class TcpClient extends TcpChannel<byte[]>{
    private final Queue<byte[]> sendQueue;
    private ByteBufferPool byteBufferPool;
    private int maximumTransmissionUnit;

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
        this.maximumTransmissionUnit = 1472;
    }

    /**
     * 建構子.
     *
     * @param tcpChannel AsynchronousSocketChannel
     */
    public TcpClient(TcpChannel tcpChannel, int maximumTransmissionUnit) throws IOException {
        super(tcpChannel);
        if(!super.isConnect())
            throw new IOException("Channel is not connected");

        this.sendQueue = new ConcurrentLinkedQueue<>();
        this.maximumTransmissionUnit = maximumTransmissionUnit;
    }

    /* **************************************************************************************
     *  Override method
     */

    @Override
    protected void onConnect(){

    }

    @Override
    protected void onConnectFail(){}

    @Override
    protected void onDisconnect(){}

    @Override
    protected void onRemoteDisconnect(){}

    @Override
    protected void onTransfer(ByteBuffer transferByteBuffer, byte[] attachment){}

    @Override
    protected void onTransferFail(ByteBuffer transferByteBuffer, byte[] attachment){}

    @Override
    protected void onReceiver(ByteBuffer receiverByteBuffer, byte[] attachment){}

    @Override
    protected void onReceiverFail(ByteBuffer receiverByteBuffer, byte[] attachment){}

    /* **************************************************************************************
     *  Public method
     */

    public boolean setMaximumTransmissionUnit(int maximumTransmissionUnit) {
        if(isConnect())
            return false;

        this.maximumTransmissionUnit = maximumTransmissionUnit;
        return true;
    }

    public int getMaximumTransmissionUnit(){
        return this.maximumTransmissionUnit;
    }
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

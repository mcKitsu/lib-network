package net.mckitsu.lib.network.tcp.handler;

import net.mckitsu.lib.network.tcp.TcpChannel;
import net.mckitsu.lib.util.encrypt.AES;
import net.mckitsu.lib.util.event.CompletionHandlerEvent;
import net.mckitsu.lib.util.pool.BufferPools;
import net.mckitsu.lib.util.pool.ByteBufferPool;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.Timer;

public abstract class HandlerTransceiver {
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */

    /* **************************************************************************************
     *  Variable <Private>
     */
    private final TcpChannel tcpChannel;
    private final ByteBufferPool byteBufferPool;
    private final Timer timer;
    private final AES aes;

    private final CompletionHandlerEvent<Integer, TransferAttachment> CompletionHandlerEventWrite
            = new CompletionHandlerEvent<>(this::handleWrite, this::handleWriteFail);

    private final CompletionHandlerEvent<Integer, ByteBuffer> completionHandlerEventRead
            = new CompletionHandlerEvent<>(this::handleRead, this::handleReadFail);



    /* **************************************************************************************
     *  Abstract method <Public>
     */

    /* **************************************************************************************
     *  Abstract method <Protected>
     */
    protected abstract void onReceiver(byte[] data);

    protected abstract void onTransfer(byte[] data);

    protected abstract void onTransceiverFail();

    /* **************************************************************************************
     *  Construct Method
     */
    public HandlerTransceiver(TcpChannel tcpChannel, AES aes, int maximumTransmissionUnit){
        this.tcpChannel = tcpChannel;
        this.aes = aes;
        this.byteBufferPool = BufferPools.newCacheBufferPool(32, maximumTransmissionUnit);
        this.timer = new Timer();
    }

    /* **************************************************************************************
     *  Public Method
     */
    public void write(byte[] data){

    }

    public void close(){
        this.timer.cancel();
    }

    /* **************************************************************************************
     *  Public Method <Override>
     */

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
    private void handleWrite(Integer result, TransferAttachment attachment){

    }

    private void handleWriteFail(Throwable throwable, TransferAttachment TransferAttachment){

    }

    private void handleRead(Integer result, ByteBuffer attachment){

        this.byteBufferPool.free(attachment);
    }

    private void handleReadFail(Throwable throwable, ByteBuffer attachment){

    }

    private void beginReceiver(){
        ByteBuffer byteBuffer = this.byteBufferPool.alloc();
        this.tcpChannel.read(byteBuffer, byteBuffer, this.completionHandlerEventRead);
    }

    private void beginTransfer(){

    }
    /* **************************************************************************************
     *  Private Method <Override>
     */

    /* **************************************************************************************
     *  Private Method <Static>
     */

    /* **************************************************************************************
     *  Private Class
     */
    private static class TransferAttachment{
        public final ByteBuffer byteBuffer;
        public final Queue<byte[]> dataList;


        public TransferAttachment(ByteBuffer byteBuffer, Queue<byte[]> dataList){
            this.byteBuffer = byteBuffer;
            this.dataList = dataList;
        }
    }
}

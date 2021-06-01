package net.mckitsu.lib.network.tcp.handler;

import com.sun.org.apache.bcel.internal.generic.FADD;
import net.mckitsu.lib.network.tcp.TcpChannel;
import net.mckitsu.lib.util.encrypt.AES;
import net.mckitsu.lib.util.event.CompletionHandlerEvent;
import net.mckitsu.lib.util.pool.BufferPools;
import net.mckitsu.lib.util.pool.ByteBufferPool;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class HandlerTransceiver implements HandlerReorganization.Event{
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */

    /* **************************************************************************************
     *  Variable <Private>
     */
    private final Queue<ByteBuffer> receiverQueue = new ConcurrentLinkedQueue<>();
    private final Queue<byte[]> transferQueue = new ConcurrentLinkedQueue<>();
    private final Queue<byte[]> writeQueue = new ConcurrentLinkedQueue<>();
    private final ByteBuffer transferBuffer = ByteBuffer.allocate(1048576);
    private final HandlerReorganization handlerReorganization = new HandlerReorganization(this);


    private TcpChannel tcpChannel;
    private ByteBufferPool byteBufferPool;
    private AES encryptAes;
    private Event event;

    private final CompletionHandlerEvent<Integer, byte[]> completionHandlerEventWrite
            = new CompletionHandlerEvent<>(this::handleEventWriteCompleted, this::handleEventWriteFail);

    private final CompletionHandlerEvent<Integer, ByteBuffer> completionHandlerEventRead
            = new CompletionHandlerEvent<>(this::handleEventReadCompleted, this::handleEventReadFail);


    private Boolean handleReceiverEventMutex = false;
    private Boolean handleTransferEventMutex = false;
    private Boolean readMutex = false;
    private Boolean writeMutex = false;

    /* **************************************************************************************
     *  Abstract method <Public>
     */

    /* **************************************************************************************
     *  Abstract method <Protected>
     */

    /* **************************************************************************************
     *  Construct Method
     */

    /* **************************************************************************************
     *  Public Method
     */
    public synchronized boolean start(TcpChannel tcpChannel, AES encryptAes, int bufferSize, Event event){
        if(this.tcpChannel != null)
            return false;

        this.tcpChannel = tcpChannel;
        this.encryptAes = encryptAes;
        this.event = event;
        this.byteBufferPool = BufferPools.newCacheBufferPool(32, bufferSize);

        this.beginRead();

        return true;
    }

    public synchronized boolean stop(){
        if(this.tcpChannel == null)
            return false;

        this.tcpChannel = null;
        this.encryptAes = null;
        this.event = null;
        this.byteBufferPool = null;

        return true;
    }

    public void write(byte[] data){
        if(data == null)
            return;

        this.writeQueue.add(data);
        this.beginWrite();
    }

    /* **************************************************************************************
     *  Public Method <Override>
     */
    @Override
    public void onHandlerReorganizationFinish(ByteBuffer byteBuffer) {
        this.event.onReceiver(this.encryptAes.decrypt(byteBuffer.array(), 0, byteBuffer.remaining()));
    }

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
    private void handleEventWriteCompleted(Integer result, byte[] attachment){
        this.transferQueue.add(attachment);
        synchronized (this.writeMutex){
            this.writeMutex = false;
        }

        this.beginWrite();
        this.handleTransferEvent();
    }

    private void handleEventWriteFail(Throwable throwable, byte[] TransferAttachment){
        Event cache = this.event;

        if(this.stop()){
            try {
                cache.onTransceiverFail();
            }catch (Throwable ignore){}
        }

    }

    private void handleEventReadCompleted(Integer result, ByteBuffer attachment){
        this.receiverQueue.add(attachment);
        this.readMutex = false;
        this.beginRead();
        this.byteBufferPool.free(attachment);
        //this.handleReceiverEvent();
    }

    private void handleEventReadFail(Throwable throwable, ByteBuffer attachment){
        Event cache = this.event;

        if(this.stop())
            cache.onTransceiverFail();
    }



    private void beginRead(){
        if(this.readMutex)
            return;

        this.readMutex = true;
        ByteBuffer byteBuffer = this.byteBufferPool.alloc();
        this.tcpChannel.read(byteBuffer, byteBuffer, this.completionHandlerEventRead);
    }

    private void beginWrite(){
        synchronized (this.writeMutex){
            if(this.writeMutex)
                return;

            this.writeMutex = true;
        }


        byte[] handleData;

        synchronized(this.writeQueue){
            if(this.writeQueue.isEmpty()){
                this.writeMutex = false;
                return;
            }
            handleData = this.writeQueue.poll();
        }

        this.tcpChannel.write(this.packetTransfer(this.transferBuffer, handleData, this.encryptAes),
                handleData,
                this.completionHandlerEventWrite);
    }

    private void handleReceiverEvent(){
        synchronized (this.handleReceiverEventMutex){
            if(this.handleReceiverEventMutex)
                return;

            this.handleReceiverEventMutex = true;
        }


        while(true){
            ByteBuffer byteBuffer;
            synchronized (this.receiverQueue){
                if(this.receiverQueue.isEmpty())
                    break;
                byteBuffer = this.receiverQueue.poll();
            }

            try {
                byteBuffer.flip();
                //this.handlerReorganization.input(byteBuffer);
                this.byteBufferPool.free(byteBuffer);
            }catch (Throwable ignore){}
        }

        synchronized (this.handleReceiverEventMutex){
            this.handleReceiverEventMutex = false;
        }
    }

    private void handleTransferEvent(){
        synchronized (this.handleTransferEventMutex){
            if(this.handleTransferEventMutex)
                return;

            this.handleTransferEventMutex = true;
        }

        while(true){
            byte[] handleData;

            synchronized (this.transferQueue){
                if(this.transferQueue.isEmpty())
                    break;
                handleData = this.transferQueue.poll();
            }

            try {
                this.event.onTransfer(handleData);
            }catch (Throwable ignore){}
        }

        synchronized (this.handleTransferEventMutex){
            this.handleTransferEventMutex = false;
        }
    }

    private ByteBuffer packetTransfer(ByteBuffer bytebuffer, byte[] sourceData, AES encryptAes){
        try{
            bytebuffer.clear();
            bytebuffer.putInt(0);
            packetWriteLength(bytebuffer, encryptAes.encrypt(sourceData, bytebuffer));
            bytebuffer.flip();
        }catch (Throwable e){
            e.printStackTrace();
        }

        return bytebuffer;
    }

    private void packetWriteLength(ByteBuffer src, int length){
        if(src.array().length<=4)
            return;

        src.array()[0] = (byte)(length >>> 24);
        src.array()[1] = (byte)(length >>> 16);
        src.array()[2] = (byte)(length >>> 8);
        src.array()[3] = (byte)(length);
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

    /* **************************************************************************************
     *  Public Interface Event
     */
    public interface Event {
        void onReceiver(byte[] data);

        void onTransfer(byte[] data);

        void onTransceiverFail();
    }

    /* **************************************************************************************
     *  Public Static Class EventEntity
     */
    public static class EventEntity implements Event{
        public Consumer<byte[]> onReceiver;
        public Consumer<byte[]> onTransfer;
        public Runnable onTransceiverFail;

        @Override
        public void onReceiver(byte[] data) {
            try{
                this.onReceiver.accept(data);
            }catch (Throwable ignore){}
        }

        @Override
        public void onTransfer(byte[] data) {
            try {
                this.onTransfer.accept(data);
            }catch (Throwable ignore){}
        }

        @Override
        public void onTransceiverFail() {
            try {
                this.onTransceiverFail.run();
            }catch (Throwable ignore){}
        }
    }
}

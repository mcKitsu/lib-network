package net.mckitsu.lib.network.local;

import net.mckitsu.lib.network.TcpChannel;
import net.mckitsu.lib.network.util.CompletionHandlerEvent;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ReadPendingException;
import java.nio.channels.WritePendingException;
import java.util.IllformedLocaleException;
import java.util.zip.CRC32;

public abstract class TcpClientTransfer {
    /* **************************************************************************************
     *  Variable <Public>
     * /

    /* **************************************************************************************
     *  Variable <Protected>
     */

    /* **************************************************************************************
     *  Variable <Private>
     */
    private final CompletionHandlerEvent<Integer, ByteBuffer> eventHeaderRead
            = new CompletionHandlerEvent<>(this::eventTransferReadHeaderCompleted, this::eventTransferReadHeaderFailed);

    private final CompletionHandlerEvent<Integer, ByteBuffer> eventRead
            = new CompletionHandlerEvent<>(this::eventTransferReadCompleted, this::eventTransferReadFailed);

    private final CompletionHandlerEvent<Integer, ByteBuffer> eventHeaderWrite
            = new CompletionHandlerEvent<>(this::eventTransferWriteHeaderCompleted, this::eventTransferWriteHeaderFailed);

    private final CompletionHandlerEvent<Integer, ByteBuffer> eventWrite
            = new CompletionHandlerEvent<>(this::eventTransferWriteCompleted, this::eventTransferWriteFailed);

    private final CRC32 checksumHandleRead = new CRC32();
    private final CRC32 checksumHandleWrite = new CRC32();
    private final TcpClientPacketHeader packetHeaderWrite = new TcpClientPacketHeader();
    private final TcpClientPacketHeader packetHeaderRead = new TcpClientPacketHeader();
    private final ByteBuffer byteBufferHeaderRead = ByteBuffer.allocate(12);
    private final ByteBuffer byteBufferHeaderWrite = ByteBuffer.allocate(12);

    private int numberRead = 0;
    private int numberWrite = 0;

    private boolean busyRead = false;
    private boolean busyWrite = false;

    private CompletionHandler<byte[], Object> readHandler;
    private CompletionHandler<byte[], Object> writeHandler;

    private Object readAttachment;
    private Object writeAttachment;

    /* **************************************************************************************
     *  Abstract method <Public>
     */

    /* **************************************************************************************
     *  Abstract method <Protected>
     */
    protected abstract TcpChannel getTcpChannel();


    /* **************************************************************************************
     *  Construct Method
     */

    /* **************************************************************************************
     *  Public Method
     */

    /* **************************************************************************************
     *  Public Method <Override>
     */

    /* **************************************************************************************
     *  Public Method <Static>
     */

    /* **************************************************************************************
     *  Protected Method
     */

    /*----------------------------------------
     *  isBusyRead
     *----------------------------------------*/
    protected boolean isBusyRead(){
        return this.busyRead;
    }


    /*----------------------------------------
     *  isBusyWrite
     *----------------------------------------*/
    protected boolean isBusyWrite(){
        return this.busyWrite;
    }


    /*----------------------------------------
     *  transferCountClear
     *----------------------------------------*/
    protected void transferCountClear(){
        this.numberRead = 0;
        this.numberWrite = 0;
    }


    /*----------------------------------------
     *  transferRead
     *----------------------------------------*/
    protected <A> void transferRead(A attachment, CompletionHandler<byte[], A> handler){
        if(this.busyRead)
            throw new ReadPendingException();

        this.busyRead = true;

        this.readHandler = (CompletionHandler<byte[], Object>) handler;
        this.readAttachment = attachment;
        this.byteBufferHeaderRead.clear();
        this.transferReadHeader(this.byteBufferHeaderRead);
    }


    /*----------------------------------------
     *  transferWrite
     *----------------------------------------*/
    protected <A> void transferWrite(byte[] data, A attachment, CompletionHandler<byte[], A> handler){
        if(this.busyWrite)
            throw new WritePendingException();

        this.busyWrite = true;
        this.writeHandler = (CompletionHandler<byte[], Object>) handler;
        this.writeAttachment = attachment;

        this.checksumHandleWrite.update(data);
        int checksum = (int)this.checksumHandleWrite.getValue();
        this.checksumHandleWrite.reset();

        this.packetHeaderWrite.setAll(this.numberWrite, checksum, data.length);

        this.byteBufferHeaderWrite.clear();
        this.byteBufferHeaderWrite.put(this.packetHeaderWrite.array());
        this.byteBufferHeaderWrite.flip();

        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        this.transferWriteHeader(this.byteBufferHeaderWrite, byteBuffer);
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

    /**
     * transferWriteSuccessful
     *
     * @param result data
     */
    private void transferWriteSuccessful(byte[] result){
        this.busyWrite = false;

        try{
            this.writeHandler.completed(result, this.writeAttachment);
        }catch (Throwable ignore){}
    }


    /**
     * transferWriteFail
     *
     * @param exc Exception
     */
    private void transferWriteFail(Throwable exc){
        this.busyWrite = false;

        try{
            this.writeHandler.failed(exc, this.writeAttachment);
        }catch (Throwable ignore){}
    }


    /**
     * transferReadSuccessful
     *
     * @param result data
     */
    private void transferReadSuccessful(byte[] result){
        this.busyRead = false;

        try{
            this.readHandler.completed(result, this.readAttachment);
        }catch (Throwable ignore){}
    }


    /**
     * transferReadFail
     *
     * @param exc Exception
     */
    private void transferReadFail(Throwable exc){
        this.busyRead = false;

        try{
            this.readHandler.failed(exc, this.readAttachment);
        }catch (Throwable ignore){}
    }



    /*----------------------------------------
     *  transferReadHeader
     *----------------------------------------*/
    private void transferReadHeader(ByteBuffer byteBuffer){
        try{
            this.getTcpChannel().read(byteBuffer, byteBuffer, this.eventHeaderRead);
        }catch (Throwable exc){
            this.transferReadFail(exc);
        }
    }


    /*----------------------------------------
     *  transferReadData
     *----------------------------------------*/
    private void transferReadData(ByteBuffer byteBuffer){
        try{
            this.getTcpChannel().read(byteBuffer, byteBuffer, this.eventRead);
        }catch (Throwable exc){
            this.transferReadFail(exc);
        }
    }


    /*----------------------------------------
     *  transferWriteHeader
     *----------------------------------------*/
    private void transferWriteHeader(ByteBuffer header, ByteBuffer data){
        try{
            this.getTcpChannel().write(header, data, this.eventHeaderWrite);
        }catch (Throwable exc){
            this.transferWriteFail(exc);
        }
    }


    /*----------------------------------------
     *  transferWriteData
     *----------------------------------------*/
    private void transferWriteData(ByteBuffer byteBuffer){
        try{
            this.getTcpChannel().write(byteBuffer, byteBuffer, this.eventWrite);
        }catch (Throwable exc){
            this.transferWriteFail(exc);
        }
    }


    /*----------------------------------------
     *  eventTransferReadHeaderCompleted
     *----------------------------------------*/
    private void eventTransferReadHeaderCompleted(Integer result, ByteBuffer attachment) {


        if(attachment.position() == attachment.capacity()){
            this.packetHeaderRead.setArray(attachment.array());

            if(this.packetHeaderRead.getNumber() == this.numberRead){
                this.transferReadData(ByteBuffer.allocate(this.packetHeaderRead.getLength()));
            }else{
                this.transferReadFail(new IllformedLocaleException("PacketNumberNotMatch"));
            }
        }else{
            this.transferReadHeader(attachment);
        }
    }


    /*----------------------------------------
     *  eventTransferReadHeaderFailed
     *----------------------------------------*/
    private void eventTransferReadHeaderFailed(Throwable exc, ByteBuffer attachment){
        this.transferReadFail(exc);
    }


    /*----------------------------------------
     *  eventTransferReadCompleted
     *----------------------------------------*/
    private void eventTransferReadCompleted(Integer result, ByteBuffer attachment) {
        if(attachment.position() == attachment.capacity()){
            this.checksumHandleRead.update(attachment.array());
            int checksum = (int)this.checksumHandleRead.getValue();
            this.checksumHandleRead.reset();

            if(this.packetHeaderRead.getChecksum() == checksum){
                this.numberRead++;
                this.busyRead = false;
                try{
                    this.readHandler.completed(attachment.array(), this.readAttachment);
                }catch (Throwable ignore){}

            }else{
                this.transferReadFail(new IllformedLocaleException("PacketChecksumNotMatch"));
            }
        }else{
            this.transferReadData(attachment);
        }
    }


    /*----------------------------------------
     *  eventTransferReadFailed
     *----------------------------------------*/
    private void eventTransferReadFailed(Throwable exc, ByteBuffer attachment){
        this.transferReadFail(exc);
    }


    /*----------------------------------------
     *  eventTransferWriteHeaderCompleted
     *----------------------------------------*/
    private void eventTransferWriteHeaderCompleted(Integer result, ByteBuffer attachment) {
        this.transferWriteData(attachment);
    }


    /*----------------------------------------
     *  eventTransferWriteHeaderFailed
     *----------------------------------------*/
    private void eventTransferWriteHeaderFailed(Throwable exc, ByteBuffer attachment){
        this.transferWriteFail(exc);
    }


    /*----------------------------------------
     *  eventTransferWriteCompleted
     *----------------------------------------*/
    private void eventTransferWriteCompleted(Integer result, ByteBuffer attachment) {
        this.numberWrite++;
        this.transferWriteSuccessful(attachment.array());
    }


    /*----------------------------------------
     *  eventTransferWriteFailed
     *----------------------------------------*/
    private void eventTransferWriteFailed(Throwable exc, ByteBuffer attachment){
        this.transferWriteFail(exc);
    }


    /* **************************************************************************************
     *  Private Method <Override>
     */

    /* **************************************************************************************
     *  Private Method <Static>
     */
}

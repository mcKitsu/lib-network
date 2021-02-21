package net.mckitsu.lib.network.tcp.handshake;

import net.mckitsu.lib.network.NetworkException;
import net.mckitsu.lib.network.NetworkExceptionList;
import net.mckitsu.lib.network.tcp.TcpChannel;
import net.mckitsu.lib.util.event.CompletionHandlerEvent;

import java.nio.ByteBuffer;

public abstract class HandshakeMtu extends Handshake<Integer, HandshakeMtu> {
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */
    protected final CompletionHandlerEvent<Integer, ByteBuffer> completionHandlerEventReadMtu
            = new CompletionHandlerEvent<>(this::handleReadMtu, this::handleReadFail);

    protected final CompletionHandlerEvent<Integer, ByteBuffer> completionHandlerEventWriteMtu
            = new CompletionHandlerEvent<>(this::handleWriteMtu, this::handleWriteFail);

    protected final int prefix = 0xA95218BA;

    protected final int maximumTransmissionUnit;
    protected int remoteMaximumTransmissionUnit;
    /* **************************************************************************************
     *  Variable <Private>
     */

    /* **************************************************************************************
     *  Abstract method <Public>
     */

    /* **************************************************************************************
     *  Abstract method <Protected>
     */
    protected abstract void handleWriteMtu(Integer result, ByteBuffer attachment);

    protected abstract void handleReadMtu(Integer result, ByteBuffer attachment);

    /* **************************************************************************************
     *  Construct Method
     */
    protected HandshakeMtu(TcpChannel tcpChannel, int maximumTransmissionUnit){
        super(tcpChannel);
        this.maximumTransmissionUnit = maximumTransmissionUnit;
        this.completionHandlerEvent = null;
    }

    /* **************************************************************************************
     *  Public Method
     */
    public int getLocalMaximumTransmissionUnit(){
        return this.maximumTransmissionUnit;
    }

    public int getRemoteMaximumTransmissionUnit(){
        return this.remoteMaximumTransmissionUnit;
    }

    public int getMaximumTransmissionUnit(){
        return getResult();
    }

    /* **************************************************************************************
     *  Public Method <Override>
     */
    @Override
    public Integer getResult(){
        return Math.min(this.remoteMaximumTransmissionUnit, this.maximumTransmissionUnit);
    }

    /* **************************************************************************************
     *  Public Method <Static>
     */

    /* **************************************************************************************
     *  Protected Method
     */
    protected ByteBuffer allocMtuPacket(int maximumTransmissionUnit){
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.putInt(this.prefix);
        byteBuffer.putInt(maximumTransmissionUnit);
        byteBuffer.flip();
        return ByteBuffer.wrap(byteBuffer.array());
    }

    protected int getPacketMtu(ByteBuffer mtuPacket){
        mtuPacket.flip();
        if(mtuPacket.remaining() != 8)
            return 0;

        int prefix = mtuPacket.getInt();
        int mtu = mtuPacket.getInt();

        if(prefix != this.prefix)
            return 0;
        return mtu;
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
    private void handleWriteFail(Throwable exc, ByteBuffer attachment){
        super.executeFail(new NetworkException(NetworkExceptionList.CHANNEL_WRITE_FAIL, exc), this);
    }

    private void handleReadFail(Throwable exc, ByteBuffer attachment){
        super.executeFail(new NetworkException(NetworkExceptionList.CHANNEL_READ_FAIL, exc), this);
    }
    /* **************************************************************************************
     *  Private Method <Override>
     */

    /* **************************************************************************************
     *  Private Method <Static>
     */
}

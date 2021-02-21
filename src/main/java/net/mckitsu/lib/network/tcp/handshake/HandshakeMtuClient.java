package net.mckitsu.lib.network.tcp.handshake;

import net.mckitsu.lib.network.NetworkException;
import net.mckitsu.lib.network.NetworkExceptionList;
import net.mckitsu.lib.network.tcp.TcpChannel;
import net.mckitsu.lib.util.event.CompletionHandlerEvent;

import java.nio.ByteBuffer;

public class HandshakeMtuClient extends HandshakeMtu {
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */

    /* **************************************************************************************
     *  Variable <Private>
     */

    /* **************************************************************************************
     *  Abstract method <Public>
     */

    /* **************************************************************************************
     *  Abstract method <Protected>
     */

    /* **************************************************************************************
     *  Abstract method
     */

    /* **************************************************************************************
     *  Construct Method
     */
    public HandshakeMtuClient(TcpChannel tcpChannel, int maximumTransmissionUnit){
        super(tcpChannel, maximumTransmissionUnit);
    }

    /* **************************************************************************************
     *  Public Method
     */

    /* **************************************************************************************
     *  Public Method <Override>
     */
    @Override
    public void accept(CompletionHandlerEvent<Integer, HandshakeMtu> completionHandlerEvent){
        if(!super.mutex(completionHandlerEvent, this))
            return;

        try{
            ByteBuffer mtuPacket = super.allocMtuPacket(super.maximumTransmissionUnit);
            super.tcpChannel.write(mtuPacket, mtuPacket, super.completionHandlerEventWriteMtu);
        }catch (Throwable throwable){
            super.executeFail(new NetworkException(NetworkExceptionList.CHANNEL_WRITE_FAIL, throwable), this);
        }
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
    @Override
    protected void handleWriteMtu(Integer result, ByteBuffer attachment){
        try{
            ByteBuffer byteBuffer = ByteBuffer.allocate(16);
            super.tcpChannel.read(byteBuffer, byteBuffer, super.completionHandlerEventReadMtu);
        }catch (Throwable e){
            super.executeFail(new NetworkException(NetworkExceptionList.CHANNEL_READ_FAIL, e), this);
        }
    }



    @Override
    protected void handleReadMtu(Integer result, ByteBuffer attachment){
        super.remoteMaximumTransmissionUnit = super.getPacketMtu(attachment);

        if(super.getResult() != 0)
            super.executeCompleted(super.getResult(), this);
        else
            super.executeFail(new NetworkException(NetworkExceptionList.PARAMETER_NOT_MATCH), this);
    }
    /* **************************************************************************************
     *  Protected Method <Static>
     */

    /* **************************************************************************************
     *  Private Method
     */

    /* **************************************************************************************
     *  Private Method <Override>
     */

    /* **************************************************************************************
     *  Private Method <Static>
     */
}

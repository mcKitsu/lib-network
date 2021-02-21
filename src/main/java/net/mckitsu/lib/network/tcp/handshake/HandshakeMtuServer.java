package net.mckitsu.lib.network.tcp.handshake;

import net.mckitsu.lib.network.NetworkException;
import net.mckitsu.lib.network.NetworkExceptionList;
import net.mckitsu.lib.network.tcp.TcpChannel;
import net.mckitsu.lib.util.event.CompletionHandlerEvent;

import java.nio.ByteBuffer;

public class HandshakeMtuServer extends HandshakeMtu {
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
     *  Construct Method
     */
    public HandshakeMtuServer(TcpChannel tcpChannel, int maximumTransmissionUnit){
        super(tcpChannel, maximumTransmissionUnit);
    }

    /* **************************************************************************************
     *  Public Method
     */

    /* **************************************************************************************
     *  Public Method <Override>
     */
    @Override
    public void accept(CompletionHandlerEvent<Integer, HandshakeMtu> completionHandlerEvent) {
        if(!super.mutex(completionHandlerEvent, this))
            return;

        //read first
        this.completionHandlerEvent = completionHandlerEvent;

        try {
            ByteBuffer mtuPacket = ByteBuffer.allocate(64);
            super.tcpChannel.read(mtuPacket, mtuPacket, this.completionHandlerEventReadMtu);
        }catch (Throwable throwable){
            super.executeFail(new NetworkException(NetworkExceptionList.CHANNEL_READ_FAIL, throwable), this);
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
        //write successful
        if(super.getResult()!=0)
            super.executeCompleted(this.getResult(), this);
        else
            super.executeFail(new NetworkException(NetworkExceptionList.PARAMETER_NOT_MATCH), this);
    }

    @Override
    protected void handleReadMtu(Integer result, ByteBuffer attachment){
        //read successful
        super.remoteMaximumTransmissionUnit = super.getPacketMtu(attachment);
        if(super.getResult() != 0){
            try {
                ByteBuffer byteBuffer = super.allocMtuPacket(super.getResult());
                super.tcpChannel.write(byteBuffer, byteBuffer, super.completionHandlerEventWriteMtu);
            }catch (Throwable throwable){
                super.executeFail(new NetworkException(NetworkExceptionList.CHANNEL_WRITE_FAIL, throwable), this);
            }
        }else{
            super.executeFail(new NetworkException(NetworkExceptionList.PARAMETER_NOT_MATCH), this);
        }
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

package net.mckitsu.lib.network;

import net.mckitsu.lib.network.local.TcpClientTransferEncrypt;
import net.mckitsu.lib.network.util.AttachmentPacket;
import net.mckitsu.lib.network.util.CompletionHandlerEvent;

import java.net.SocketAddress;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.ShutdownChannelGroupException;


/**
 * TCP Asynchronous Client.
 *
 * @author  ZxyKira
 */
public class TcpClient extends TcpClientTransferEncrypt {
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */

    /* **************************************************************************************
     *  Variable <Private>
     */
    private final CompletionHandler<Void, AttachmentPacket<Void, Object>> eventConnect
            = new CompletionHandlerEvent<>(this::eventConnectCompleted
            , this::eventConnectFailed);

    private final CompletionHandlerEvent<Void, AttachmentPacket<Void, Object>> eventHandshake
            = new CompletionHandlerEvent<>(this::eventHandshakeCompleted
            , this::eventHandshakeFailed);

    private TcpChannel tcpChannel;


    /* **************************************************************************************
     *  Abstract method <Public>
     */

    /* **************************************************************************************
     *  Abstract method <Protected>
     */

    /* **************************************************************************************
     *  Construct Method
     */
    /**
     * construct.
     */
    public TcpClient(){
        this.tcpChannel = null;
    }


    /* **************************************************************************************
     *  Public Method
     */

    /* **************************************************************************************
     *  Public Method <Override>
     */

    /**
     * 建立連線
     *
     * @param remoteAddress 連線目標位置。
     * @param attachment 附件。
     * @param handler 事件完成處理器。
     * @param <A> 附件類型。
     * @throws AlreadyConnectedException 原始類已連至其他目標。
     */
    public <A> void connect(SocketAddress remoteAddress, A attachment, CompletionHandler<Void, A> handler){
        if(this.tcpChannel == null)
            this.tcpChannel = new TcpChannel();

        if(this.tcpChannel.isConnect())
            throw new AlreadyConnectedException();

        AttachmentPacket<Void, A> attachmentPacket = new AttachmentPacket<>(attachment, null, handler);
        this.tcpChannel.connect(remoteAddress, attachmentPacket, (CompletionHandler)this.eventConnect);
    }


    /**
     * 建立連線，依附已經連線的TcpChannel。
     *
     * @param tcpChannel 已經連線的TcpChannel。
     * @param attachment 附件。
     * @param handler 事件完成處理器。
     * @param <A> 附件類型。
     * @throws ConnectionPendingException 原始類已連至其他目標。
     * @throws ShutdownChannelGroupException 輸入TcpChannel並未連線至目標。
     */
    public <A> void connect(TcpChannel tcpChannel, A attachment, CompletionHandler<Void, A> handler){
        if(this.tcpChannel != null){
            if(this.tcpChannel.isConnect())
                throw new ConnectionPendingException();
        }

        if(!tcpChannel.isConnect())
            throw new ShutdownChannelGroupException();


        this.tcpChannel = tcpChannel;
        AttachmentPacket<Void, A> attachmentPacket = new AttachmentPacket<>(attachment, null, handler);

        this.startHandshakeMaster(attachmentPacket, (CompletionHandler)this.eventHandshake);
    }


    /**
     * 關閉連線
     */
    public void disconnect(){
        try {
            if(this.tcpChannel.isConnect())
                this.tcpChannel.close();

        }catch (Throwable ignore){}
    }


    /**
     *
     * @param data write data
     * @param attachment user attachment
     * @param handler CompletionHandler
     * @param <A> user attachment type
     */
    public <A> void write(byte[] data, A attachment, CompletionHandler<byte[], A> handler){
        super.transferWrite(data, attachment, handler);
    }


    /**
     *
     * @param attachment user attachment
     * @param handler CompletionHandler
     * @param <A> user attachment type
     */
    public <A> void read(A attachment, CompletionHandler<byte[], A> handler){
        super.transferRead(attachment, handler);
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

    /**
     * getTcpChannel.
     *
     * @return TcpChannel
     */
    @Override
    protected TcpChannel getTcpChannel(){
        return this.tcpChannel;
    }


    /* **************************************************************************************
     *  Protected Method <Static>
     */

    /* **************************************************************************************
     *  Private Method
     */

    /**
     * eventHandshakeCompleted
     *
     * @param result Void
     * @param attachment Void
     */
    private void eventHandshakeCompleted(Void result, AttachmentPacket<Void, Object> attachment){
        try{
            attachment.handler.completed(null, attachment.attachment);
        }catch (Throwable ignore){}
    }


    /**
     * eventHandshakeFailed
     *
     * @param exc Exception
     * @param attachment Void
     */
    private void eventHandshakeFailed(Throwable exc, AttachmentPacket<Void, Object> attachment){
        try{
            attachment.handler.failed(exc, attachment.attachment);
        }catch (Throwable ignore){}
    }


    /**
     * eventConnectCompleted.
     *
     * @param result Void
     * @param attachment Void
     */
    private void eventConnectCompleted(Void result, AttachmentPacket<Void, Object> attachment){
        this.startHandshakeSlave(attachment, this.eventHandshake);
    }


    /**
     * eventConnectFailed
     *
     * @param exc Exception
     * @param attachment Void
     */
    private void eventConnectFailed(Throwable exc, AttachmentPacket<Void, Object> attachment){
        try{
            attachment.handler.failed(exc, attachment.attachment);
        }catch (Throwable ignore){}
    }

    /* **************************************************************************************
     *  Private Method <Override>
     */

    /* **************************************************************************************
     *  Private Method <Static>
     */

    /* **************************************************************************************
     *  Class/Interface/Enum
     */
}

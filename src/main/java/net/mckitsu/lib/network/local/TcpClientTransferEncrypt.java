package net.mckitsu.lib.network.local;

import net.mckitsu.lib.network.util.AttachmentPacket;
import net.mckitsu.lib.network.util.CompletionHandlerEvent;
import net.mckitsu.lib.network.util.EncryptAes;

import java.nio.channels.CompletionHandler;

public abstract class TcpClientTransferEncrypt extends TcpClientTransfer {
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */

    /* **************************************************************************************
     *  Variable <Private>
     */
    private final CompletionHandler<byte[], AttachmentPacket<byte[], Object>> eventTransferEncryptRead
            = new CompletionHandlerEvent<>(this::eventTransferEncryptReadCompletion
            , this::eventTransferEncryptReadFailed);

    private final CompletionHandler<byte[], AttachmentPacket<byte[], Object>> eventTransferEncryptWrite
            = new CompletionHandlerEvent<>(this::eventTransferEncryptWriteCompletion
            , this::eventTransferEncryptWriteFailed);

    private final CompletionHandler<EncryptAes, AttachmentPacket<Object, Object>> eventHandshake
            = new CompletionHandlerEvent<>(this::eventHandshakeCompletion
            , this::eventHandshakeFailed);

    private EncryptAes encryptAes;
    private boolean handshaking = false;

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

    /* **************************************************************************************
     *  Public Method <Override>
     */

    /* **************************************************************************************
     *  Public Method <Static>
     */

    /* **************************************************************************************
     *  Protected Method
     */

    /**
     * startHandshakeMaster
     */
    protected <A> void startHandshakeMaster(A attachment, CompletionHandler<Void, A> handler){
        if(this.handshaking)
            throw new IllegalStateException();

        this.handshaking = true;
        Handshake handshake = new HandshakeMaster();
        handshake.action(this
                , new AttachmentPacket<>(attachment, null, handler)
                , (CompletionHandler)this.eventHandshake);
    }


    /**
     * startHandshakeSlave
     */
    protected <A> void startHandshakeSlave(A attachment, CompletionHandler<Void, A> handler){
        if(this.handshaking)
            throw new IllegalStateException();

        this.handshaking = true;
        Handshake handshake = new HandshakeSlave();
        handshake.action(this
                , new AttachmentPacket<>(attachment, null, handler)
                , (CompletionHandler)this.eventHandshake);
    }


    /**
     * transferDirectWrite
     *
     * @param attachment user attachment.
     * @param handler CompletionHandler.
     * @param <A> user attachment type.
     */
    protected <A> void transferDirectRead(A attachment, CompletionHandler<byte[], A> handler){
        super.transferRead(attachment, handler);
    }


    /**
     * transferDirectWrite
     *
     * @param data write data.
     * @param attachment user attachment.
     * @param handler CompletionHandler.
     * @param <A> user attachment type.
     */
    protected <A> void transferDirectWrite(byte[] data, A attachment, CompletionHandler<byte[], A> handler){
        super.transferWrite(data, attachment, handler);
    }

    /* **************************************************************************************
     *  Protected Method <Override>
     */

    /**
     *
     * @param attachment user attachment.
     * @param handler CompletionHandler.
     * @param <A> user attachment type.
     */
    @Override
    protected <A> void transferRead(A attachment, CompletionHandler<byte[], A> handler){
        AttachmentPacket<byte[], Object> att = new AttachmentPacket<>(attachment, null, (CompletionHandler)handler);
        super.transferRead(att, this.eventTransferEncryptRead);
    }


    /**
     *
     * @param data write data.
     * @param attachment user attachment.
     * @param handler CompletionHandler.
     * @param <A> user attachment type.
     */
    @Override
    protected <A> void transferWrite(byte[] data, A attachment, CompletionHandler<byte[], A> handler){
        try {
            byte[] encryptData = encryptAes.encrypt(data);
            AttachmentPacket<byte[], Object> att = new AttachmentPacket<>(attachment, data, (CompletionHandler)handler);
            super.transferWrite(encryptData, att, this.eventTransferEncryptWrite);
        } catch (Throwable exc) {
            handler.failed(exc, attachment);
        }
    }

    /* **************************************************************************************
     *  Protected Method <Static>
     */

    /* **************************************************************************************
     *  Private Method
     */

    /**
     * eventHandshakeCompletion
     *
     * @param encryptAes encrypt aes entity
     * @param attachment user attachment
     */
    private void eventHandshakeCompletion(EncryptAes encryptAes, AttachmentPacket<Object, Object> attachment){
        this.handshaking = false;
        this.encryptAes = encryptAes;

        try {
            attachment.handler.completed(null, attachment.attachment);
        }catch (Throwable ignore){}
    }


    /**
     * eventHandshakeFailed
     *
     * @param exc Exception
     * @param attachment user attachment
     */
    private void eventHandshakeFailed(Throwable exc, AttachmentPacket<Object, Object> attachment){
        this.handshaking = false;

        try{
            attachment.handler.failed(exc, attachment.attachment);
        }catch (Throwable ignore){}
    }


    /**
     * eventTransferEncryptReadCompletion
     *
     * @param result byte array.
     * @param attachment user attachment.
     */
    private void eventTransferEncryptReadCompletion(byte[] result, AttachmentPacket<byte[], Object> attachment){
        try{
            byte[] data = this.encryptAes.decrypt(result);
            try{
                attachment.handler.completed(data, attachment.attachment);
            }catch (Throwable ignore){}
        }catch (Throwable exc){
            attachment.handler.failed(exc, attachment.attachment);
        }
    }


    /**
     * eventTransferEncryptReadFailed
     *
     * @param exc Exception
     * @param attachment user attachment
     */
    private void eventTransferEncryptReadFailed(Throwable exc, AttachmentPacket<byte[], Object> attachment){
        try{
            attachment.handler.failed(exc, attachment.attachment);
        }catch (Throwable ignore){}
    }


    /**
     * eventTransferEncryptWriteCompletion
     *
     * @param data byte[]
     * @param attachment user attachment
     */
    private void eventTransferEncryptWriteCompletion(byte[] data, AttachmentPacket<byte[], Object> attachment){
        try {
            attachment.handler.completed(attachment.result, attachment.attachment);
        }catch (Throwable ignore){}
    }


    /**
     * eventTransferEncryptWriteFailed
     *
     * @param exc Exception
     * @param attachment user attachment
     */
    private void eventTransferEncryptWriteFailed(Throwable exc, AttachmentPacket<byte[], Object> attachment){
        try {
            attachment.handler.failed(exc, attachment.attachment);
        }catch (Throwable ignore){}
    }


    /* **************************************************************************************
     *  Private Method <Override>
     */

    /* **************************************************************************************
     *  Private Method <Static>
     */
}

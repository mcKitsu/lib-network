package net.mckitsu.lib.network.local;

import net.mckitsu.lib.network.util.CompletionHandlerEvent;
import net.mckitsu.lib.network.util.EncryptAes;

import java.nio.channels.CompletionHandler;
import java.util.logging.Logger;

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
    private final CompletionHandler<byte[], Attachment<byte[], Object>> eventTransferEncryptRead
            = new CompletionHandlerEvent<>(this::eventTransferEncryptReadCompletion
            , this::eventTransferEncryptReadFailed);

    private final CompletionHandler<byte[], Attachment<byte[], Object>> eventTransferEncryptWrite
            = new CompletionHandlerEvent<>(this::eventTransferEncryptWriteCompletion
            , this::eventTransferEncryptWriteFailed);

    private final CompletionHandler<EncryptAes, Attachment<Object, Object>> eventHandshake
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
                , new Attachment<>(handler, attachment, null)
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
                , new Attachment<>(handler, attachment, null)
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
        Attachment<byte[], Object> att = new Attachment<>((CompletionHandler<byte[], Object>) handler, attachment, null);
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
            Attachment<byte[], Object> att = new Attachment<>((CompletionHandler<byte[], Object>) handler, attachment, data);
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
    private void eventHandshakeCompletion(EncryptAes encryptAes, Attachment<Object, Object> attachment){
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
    private void eventHandshakeFailed(Throwable exc, Attachment<Object, Object> attachment){
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
    private void eventTransferEncryptReadCompletion(byte[] result, Attachment<byte[], Object> attachment){
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
    private void eventTransferEncryptReadFailed(Throwable exc, Attachment<byte[], Object> attachment){
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
    private void eventTransferEncryptWriteCompletion(byte[] data, Attachment<byte[], Object> attachment){
        try {
            attachment.handler.completed(attachment.data, attachment.attachment);
        }catch (Throwable ignore){}
    }


    /**
     * eventTransferEncryptWriteFailed
     *
     * @param exc Exception
     * @param attachment user attachment
     */
    private void eventTransferEncryptWriteFailed(Throwable exc, Attachment<byte[], Object> attachment){
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

    protected static class Attachment<V, A>{
        public final CompletionHandler<V, A> handler;
        public final A attachment;
        public final V data;

        public Attachment(CompletionHandler<V, A> handler, A attachment, V data){
            this.handler =handler;
            this.attachment = attachment;
            this.data = data;
        }
    }
}

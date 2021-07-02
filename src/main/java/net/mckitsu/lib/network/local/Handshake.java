package net.mckitsu.lib.network.local;

import net.mckitsu.lib.network.util.EncryptAes;
import net.mckitsu.lib.network.util.EncryptRsa;

import java.nio.channels.CompletionHandler;


public abstract class Handshake {
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */
    protected boolean isAction;
    protected CompletionHandler<EncryptAes, Object> handler;
    protected Object attachment;
    protected TcpClientTransferEncrypt tcpClientTransferEncrypt;

    protected EncryptAes encryptAes;
    protected EncryptRsa encryptRsa;

    /* **************************************************************************************
     *  Variable <Private>
     */

    /* **************************************************************************************
     *  Abstract method <Public>
     */

    /**
     *
     * @param tcpClientTransferEncrypt network transfer entity.
     * @param attachment user attachment.
     * @param handler compiler handler.
     * @param <A> user attachment type.
     */
    public <A> void action(TcpClientTransferEncrypt tcpClientTransferEncrypt, A attachment, CompletionHandler<EncryptAes, A> handler){
        if(this.isAction)
            throw new IllegalStateException();

        this.isAction = true;
        this.handler = (CompletionHandler<EncryptAes, Object>) handler;
        this.attachment = attachment;
        this.tcpClientTransferEncrypt = tcpClientTransferEncrypt;
    }


    /* **************************************************************************************
     *  Abstract method <Protected>
     */

    /* **************************************************************************************
     *  Construct Method
     */

    /**
     * construct
     *
     */
    protected Handshake(){
        this.isAction = false;
    }


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
     *
     * @param encryptAes handshake result.
     */
    protected void doCompleted(EncryptAes encryptAes){
        this.isAction = false;
        try {
            this.handler.completed(encryptAes, this.attachment);
        }catch (Throwable ignore){}

        this.tcpClientTransferEncrypt = null;
        this.encryptAes = null;
        this.encryptRsa = null;
    }


    /**
     *
     * @param exc handshake exception
     */
    protected void doFailed(Throwable exc){
        this.isAction = false;
        try {
            this.handler.failed(exc, this.attachment);
        }catch (Throwable ignore){}

        this.tcpClientTransferEncrypt = null;
        this.encryptAes = null;
        this.encryptRsa = null;
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

    /* **************************************************************************************
     *  Private Method <Override>
     */

    /* **************************************************************************************
     *  Private Method <Static>
     */
}

package net.mckitsu.lib.network.util;

import java.nio.channels.CompletionHandler;

public class AttachmentPacket<V, A> {
    /* **************************************************************************************
     *  Variable <Public>
     */
    public final CompletionHandler<V, A> handler;
    public final A attachment;
    public final V result;


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

    /**
     * construct
     *
     * @param attachment Attachment
     * @param result Result
     * @param handler Handler
     */
    public AttachmentPacket(A attachment, V result, CompletionHandler<V, A> handler){
        this.handler =handler;
        this.attachment = attachment;
        this.result = result;
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

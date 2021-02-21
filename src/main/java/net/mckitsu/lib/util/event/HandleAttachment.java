package net.mckitsu.lib.util.event;

import java.nio.ByteBuffer;

public class HandleAttachment<R, T> {
    public CompletionHandlerEvent<R, T> completionHandlerEvent;
    public ByteBuffer byteBuffer;
    public T attachment;

    public HandleAttachment(ByteBuffer byteBuffer, CompletionHandlerEvent<R, T> completionHandlerEvent){
        this.byteBuffer = byteBuffer;
        this.completionHandlerEvent = completionHandlerEvent;
        this.attachment = null;
    }

    public HandleAttachment(ByteBuffer byteBuffer, T attachment, CompletionHandlerEvent<R, T> completionHandlerEvent){
        this.byteBuffer = byteBuffer;
        this.completionHandlerEvent = completionHandlerEvent;
        this.attachment = attachment;
    }

    public HandleAttachment(T attachment, CompletionHandlerEvent<R, T> completionHandlerEvent){
        this.byteBuffer = null;
        this.completionHandlerEvent = completionHandlerEvent;
        this.attachment = attachment;
    }
}

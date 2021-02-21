package net.mckitsu.lib.network.tcp.handshake;

import net.mckitsu.lib.network.NetworkException;
import net.mckitsu.lib.network.NetworkExceptionList;
import net.mckitsu.lib.network.tcp.TcpChannel;
import net.mckitsu.lib.util.encrypt.AES;
import net.mckitsu.lib.util.event.CompletionHandlerEvent;

import java.util.function.Consumer;

public abstract class Handshake<R, T> implements Consumer<CompletionHandlerEvent<R, T>> {
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */
    protected final TcpChannel tcpChannel;

    protected CompletionHandlerEvent<R, T> completionHandlerEvent;

    /* **************************************************************************************
     *  Variable <Private>
     */

    /* **************************************************************************************
     *  Abstract method <Public>
     */
    public abstract R getResult();

    /* **************************************************************************************
     *  Abstract method <Protected>
     */

    /* **************************************************************************************
     *  Construct Method
     */
    protected Handshake(TcpChannel tcpChannel){
        this.tcpChannel = tcpChannel;
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
    protected void executeFail(Throwable throwable, T attachment){
        CompletionHandlerEvent<R, T> cache = this.completionHandlerEvent;
        this.completionHandlerEvent = null;
        try {
            cache.failed(throwable, attachment);
        }catch (Throwable ignore){}
    }

    protected void executeCompleted(R result, T attachment){
        CompletionHandlerEvent<R, T> cache = this.completionHandlerEvent;
        this.completionHandlerEvent = null;
        try {
            cache.completed(result, attachment);
        }catch (Throwable ignore){}
    }

    protected boolean mutex(CompletionHandlerEvent<R, T> completionHandlerEvent, T attachment){
        if(this.completionHandlerEvent!=null){
            try{
                completionHandlerEvent.failed(new NetworkException(NetworkExceptionList.ILLEGAL_ACCESS), attachment);
            }catch (Throwable ignore){}
            return false;
        }

        this.completionHandlerEvent = completionHandlerEvent;
        return true;
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

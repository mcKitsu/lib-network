package net.mckitsu.lib.network.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class SynchronizeExecute<A> {
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */

    /* **************************************************************************************
     *  Variable <Private>
     */
    private final Consumer<A> consumer;
    private final Queue<A> queue = new ConcurrentLinkedQueue<>();
    private boolean executing = false;


    /* **************************************************************************************
     *  Abstract method <Public>
     */

    /* **************************************************************************************
     *  Abstract method <Protected>
     */

    /* **************************************************************************************
     *  Construct Method
     */
    public SynchronizeExecute(Consumer<A> consumer){
        this.consumer = consumer;
    }


    /* **************************************************************************************
     *  Public Method
     */

    /*----------------------------------------
     *  execute
     *----------------------------------------*/
    public void execute(A attachment){
        synchronized (this.queue) {
            this.queue.add(attachment);
        }

        this.action();
    }


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

    /*----------------------------------------
     *  action
     *----------------------------------------*/
    private void action(){
        if(this.executing)
            return;

        while(true){
            A attachment;
            synchronized (this.queue){
                attachment = this.queue.poll();
            }

            if(attachment == null){
                this.executing = false;
                break;
            }

            try {
                this.consumer.accept(attachment);
            }catch (Throwable ignore){}
        }
    }

    /* **************************************************************************************
     *  Private Method <Override>
     */

    /* **************************************************************************************
     *  Private Method <Static>
     */
}

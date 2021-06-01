package net.mckitsu.lib.network.tcp.handler;

import java.nio.ByteBuffer;

public class HandlerReorganization {
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */

    /* **************************************************************************************
     *  Variable <Private>
     */
    private final Event event;
    private final ByteBuffer byteBuffer = ByteBuffer.allocate(16777216);
    private int seasonLength;

    /* **************************************************************************************
     *  Abstract method <Public>
     */

    /* **************************************************************************************
     *  Abstract method <Protected>
     */

    /* **************************************************************************************
     *  Construct Method
     */
    public HandlerReorganization(Event event){
        this.event = event;
        this.seasonLength = 0;
    }

    /* **************************************************************************************
     *  Public Method
     */
    public synchronized void input(ByteBuffer input){
        if(this.seasonLength==0){
            this.seasonStart(input.getInt());
        }

        if(this.getLack() >= input.remaining()){
            this.byteBuffer.put(input);
            if(this.getLack() == 0){

                this.byteBuffer.flip();
                this.seasonLength = 0;
                this.event.onHandlerReorganizationFinish(this.byteBuffer);
            }
        }else{
            byte[] cache = new byte[this.getLack()];
            input.get(cache);
            this.byteBuffer.put(cache);
            this.byteBuffer.flip();
            this.seasonLength = 0;
            this.event.onHandlerReorganizationFinish(this.byteBuffer);
            this.input(input);
        }
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
    private void seasonStart(int length){
        this.byteBuffer.clear();
        this.seasonLength = length;
    }

    private int getLack(){
        System.out.println("getLack: " + (this.seasonLength - this.byteBuffer.position()));
        return this.seasonLength - this.byteBuffer.position();
    }


    /* **************************************************************************************
     *  Private Method <Override>
     */

    /* **************************************************************************************
     *  Private Method <Static>
     */

    /* **************************************************************************************
     *  Public Interface Event
     */

    public interface Event{
        void onHandlerReorganizationFinish(ByteBuffer byteBuffer);
    }
}

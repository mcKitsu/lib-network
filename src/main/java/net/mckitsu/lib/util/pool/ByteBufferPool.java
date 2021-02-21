package net.mckitsu.lib.util.pool;

import java.nio.ByteBuffer;

public class ByteBufferPool extends BufferPool<ByteBuffer>{
    private final int bufferSize;
    /* **************************************************************************************
     *  Abstract method
     */

    /* **************************************************************************************
     *  Construct method
     */
    protected ByteBufferPool(int min, int max, int bufferSize) {
        super(max, min);
        this.bufferSize = bufferSize;
        super.poolInit();
    }
    /* **************************************************************************************
     *  Override method
     */
    @Override
    protected ByteBuffer allocElement() {
        return ByteBuffer.allocate(this.bufferSize);
    }

    @Override
    protected void freeElement(ByteBuffer element) {
    }

    @Override
    protected ByteBuffer resetElement(ByteBuffer element) {
        if(element != null){
            element.clear();
        }
        return element;
    }
    /* **************************************************************************************
     *  Public method
     */

    public int getBufferSize(){
        return this.bufferSize;
    }
    /* **************************************************************************************
     *  protected method
     */

    /* **************************************************************************************
     *  Private method
     */
}
